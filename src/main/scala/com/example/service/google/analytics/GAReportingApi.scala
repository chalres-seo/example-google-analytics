package com.example.service.google.analytics

import java.util.Collections

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting
import com.google.api.services.analyticsreporting.v4.model._
import com.example.service.google.analytics.GAReportingHandler.GAReportingHandlerType
import com.example.service.google.api.GoogleApi
import com.example.service.google.credential.GoogleCredentialFactory
import com.example.utils.{AppConfig, JsonUtils}
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime

import scala.annotation.tailrec
import scala.collection.JavaConversions._

class GAReportingApi(analyticsReporting: AnalyticsReporting) extends LazyLogging with GoogleApi {
  private val reportFetchSize = AppConfig.defaultGoogleAnalyticsReportFetchSize

  private val delimiter = ","

  /**
    * query json string is base on [https://ga-dev-tools.appspot.com/query-explorer/]
    *
    * @param queryJsonString
    *                        - You can supply a maximum of 10 metrics for any query. (comma delimiter)
    *                        - You can supply a maximum of 7 dimensions in any query. (comma delimiter)
    *                        - You can supply a maximum of 1 segment in any query. (comma delimiter)
    *                        - Filter expressions Maximum length of 128 characters
    */
  def reportRequest(queryJsonString: String)(reportHandler: GAReportingHandlerType): (Int, Int) = {
    logger.info(s"report request. json info: $queryJsonString")
    val gaQuery = JsonUtils.jsonStringToMap(queryJsonString)

    this.reportRequest(gaQuery("ids"),
      gaQuery("start_date"),
      gaQuery("end_date"),
      gaQuery("metrics").split(delimiter).toVector.slice(0, 10),
      Option(gaQuery.getOrDefault("dimensions", null)).map(_.split(delimiter).toVector.slice(0, 7)),
      Option(gaQuery.getOrDefault("segment", null)))(reportHandler)

  }

  def reportRequest(ids: String,
                    startDate: String,
                    endDate: String,
                    metricExpressions: Vector[String],
                    dimensionExpressions: Option[Vector[String]],
                    segmentExpression: Option[String])
                   (reportHandler: GAReportingHandlerType): (Int, Int) = {
    val reportRequest = new ReportRequest()
    reportRequest.setViewId(ids)
    reportRequest.setDateRanges(Collections.singletonList(this.createDateRange(startDate, endDate)))
    reportRequest.setMetrics(this.createMetrics(metricExpressions))
    reportRequest.setPageSize(reportFetchSize)

    dimensionExpressions.foreach(expr => reportRequest.setDimensions(this.createDimensions(expr)))
    segmentExpression.foreach(expr => reportRequest.setSegments(Collections.singletonList(this.createSegment(expr))))

    this.reportRequest(reportRequest)(reportHandler)
  }

  /** @return requestCount, failedHandlerCount */
  private def reportRequest(reportRequest: ReportRequest)(reportHandler: GAReportingHandlerType): (Int, Int) = {
    logger.info(s"report request. request info:\n|\t${reportRequest.mkString("\n|\t")}")

    val getReportsRequest: GetReportsRequest = new GetReportsRequest()

    @tailrec
    def loop(reportRequest: ReportRequest, requestCount: Int, failedHandlerCount: Int, currentPageToken: String): (Int, Int) = {
      getReportsRequest.setReportRequests(Collections.singletonList(reportRequest))

      val reportsResponse: GetReportsResponse = this.call(analyticsReporting.reports().batchGet(getReportsRequest))(_.execute())
      val report = reportsResponse.getReports.head

      val succeedHandler = reportHandler(report, currentPageToken)

      if (succeedHandler) {
        logger.debug(s"succeed report handler. current page token: $currentPageToken")
      } else {
        logger.error(s"failed report handler. current page token: $currentPageToken")
      }

      if (report.getNextPageToken != null) {
        val nextPageToken = report.getNextPageToken
        logger.info(s"report has next page token. token: $nextPageToken")
        reportRequest.setPageToken(nextPageToken)

        loop(reportRequest, requestCount + 1, if(succeedHandler) failedHandlerCount else failedHandlerCount + 1, nextPageToken)
      } else (requestCount, failedHandlerCount)
    }

    loop(reportRequest, 1, 0, "0")
  }

  private def createDateRange(startDate: String, endDate: String): DateRange = {
    logger.debug(s"create date range. start date: $startDate, end date: $endDate")
    val dateRange = new DateRange()
    dateRange.setStartDate(startDate)
    dateRange.setEndDate(endDate)

    dateRange
  }

  private def createDateRange(startDate: DateTime, endDate: DateTime): DateRange = {
    this.createDateRange(startDate.toString("yyyy-MM-dd"), endDate.toString("yyyy-MM-dd"))
  }

  private def createMetrics(expressions: Vector[String]): Vector[Metric] = {
    logger.debug(s"create metrics. metric expressions: ${expressions.mkString(",")}")
    expressions.map(this.createMetric)
  }

  private def createMetric(expression: String, alias: Option[String]): Metric = {
    logger.debug(s"create metric. metric expression: $expression")
    val metric = new Metric()
    metric.setExpression(expression)

    if (alias.isDefined) {
      metric.setAlias(alias.get)
    }
    metric
  }

  private def createMetric(expression: String): Metric = {
    this.createMetric(expression, None)
  }

  private def createDimensions(dimensionIds: Vector[String]): Vector[Dimension] = {
    logger.debug(s"create dimensions. dimensionIds: ${dimensionIds.mkString(",")}")
    dimensionIds.map(this.createDimension)
  }

  private def createDimension(expression: String): Dimension = {
    logger.debug(s"create dimension. dimension expression: $expression")
    val dimension = new Dimension()
    dimension.setName(expression)

    dimension
  }

  private def createSegment(segmentExpression: String): Segment = {
    logger.debug(s"create segment. segment expression: $segmentExpression")
    val segment = new Segment()
    segment.setSegmentId(segmentExpression)

    segment
  }
}

object GAReportingApi extends LazyLogging {
  private val applicationName = AppConfig.applicationName
  private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
  private val jsonFactory = JacksonFactory.getDefaultInstance

  def apply(analyticsReporting: AnalyticsReporting): GAReportingApi = {
    logger.info("create google analytics reporting object")
    new GAReportingApi(analyticsReporting)
  }

  def apply(googleCredential: GoogleCredential): GAReportingApi = {
    this.apply(this.createAnalyticsReporting(googleCredential))
  }

  def apply(googleCredentialJsonString: String): GAReportingApi = {
    this.apply(this.createAnalyticsReporting(googleCredentialJsonString))
  }

  private def createAnalyticsReporting(googleCredential: GoogleCredential): AnalyticsReporting = {
    logger.info("create google analytics reporting api service client.")
    new AnalyticsReporting.Builder(
      httpTransport,
      jsonFactory,
      googleCredential
    ).setApplicationName(applicationName)
      .build()
  }

  private def createAnalyticsReporting(googleCredentialJsonString: String): AnalyticsReporting = {
    this.createAnalyticsReporting(GoogleCredentialFactory.createAnalyticsReportingCredential(googleCredentialJsonString))
  }
}