package com.example.service.google.analytics

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.services.analytics.Analytics
import com.google.api.services.analytics.model._
import com.example.service.google.api.GoogleApi
import com.example.service.google.credential.GoogleCredentialFactory
import com.example.utils.AppConfig
import com.typesafe.scalalogging.LazyLogging

import scala.annotation.tailrec
import scala.collection.JavaConversions._

class GAApi(analytics: Analytics) extends LazyLogging with GoogleApi {
  def getAccountSummaries: Vector[AccountSummary] = {
    logger.info("get account summaries.")

    @tailrec
    def loop(requestAccountSummaries: Analytics#Management#AccountSummaries#List, items: Vector[AccountSummary]): Vector[AccountSummary] = {
      val result: AccountSummaries = this.call(requestAccountSummaries)(_.execute())

      if (result.getNextLink == null) {
        items ++ result.getItems.toVector
      } else {
        loop(requestAccountSummaries.setStartIndex(result.getItems.size() + 1), items ++ result.getItems.toVector)
      }
    }
    loop(analytics.management().accountSummaries().list(), Vector.empty[AccountSummary])
  }

  def getAllAccountInfo: Vector[GAApi.AccountInfo] = {
    logger.info("get all account info.")
    this.getAccountSummaries.flatMap( summary =>
      summary.getWebProperties.flatMap(webProperty =>
        webProperty.getProfiles.map(profile =>
          GAApi.AccountInfo(summary.getId,
            summary.getName,
            webProperty.getId,
            webProperty.getInternalWebPropertyId,
            webProperty.getName,
            profile.getId,
            profile.getName)
        )))
  }

  def getAllAccountIdAndWebProperty: Vector[(String, String)] = {
    logger.info("get all account id and web property.")
    this.getAccountSummaries.flatMap( summary =>
      summary.getWebProperties.map( webProperty =>
        (summary.getId, webProperty.getId)
      ))
  }

  def getCustomDimensionInfo(accountId: String, webProperty: String): Vector[GAApi.CustomDimensionInfo] = {
    logger.info(s"get custom dimension info. accountId: $accountId, web property: $webProperty")
    @tailrec
    def loop(requestCustomDimensions: Analytics#Management#CustomDimensions#List, items: Vector[GAApi.CustomDimensionInfo]): Vector[GAApi.CustomDimensionInfo] = {
      //val result: CustomDimensions = this.apiCall(requestCustomDimensions)(_.execute())
      val result: CustomDimensions = this.call(requestCustomDimensions)(_.execute())

      if (result.getNextLink == null) {
        items ++ result.getItems.map(this.toCustomDimensionInfo)
      } else {
        loop(requestCustomDimensions.setStartIndex(result.getItems.size() + 1), items ++ result.getItems.map(this.toCustomDimensionInfo))
      }
    }
    loop(analytics.management().customDimensions().list(accountId, webProperty), Vector.empty[GAApi.CustomDimensionInfo])
  }

  private def toCustomDimensionInfo(customDimension: CustomDimension): GAApi.CustomDimensionInfo = {
    GAApi.CustomDimensionInfo(customDimension.getAccountId,
      customDimension.getWebPropertyId,
      customDimension.getId,
      customDimension.getIndex,
      customDimension.getName,
      customDimension.getActive,
      customDimension.getCreated,
      customDimension.getUpdated)
  }

  def getAllCustomDimensionInfo: Vector[GAApi.CustomDimensionInfo] = {
    logger.info("get all custom dimension info.")
    this.getAllAccountIdAndWebProperty.flatMap { case (accountId, webProperty) =>
      this.getCustomDimensionInfo(accountId, webProperty)
    }
  }

  def getCustomMetricInfo(accountId: String, webProperty: String): Vector[GAApi.CustomMetricInfo] = {
    logger.info(s"get custom metric info. accountId: $accountId, web property: $webProperty")
    @tailrec
    def loop(requestCustomMetrics: Analytics#Management#CustomMetrics#List, items: Vector[GAApi.CustomMetricInfo]): Vector[GAApi.CustomMetricInfo] = {
      //val result: CustomMetrics = this.apiCall(requestCustomMetrics)(_.execute())
      val result: CustomMetrics = this.call(requestCustomMetrics)(_.execute())

      if (result.getNextLink == null) {
        items ++ result.getItems.map(this.toCustomMetricInfo)
      } else {
        loop(requestCustomMetrics.setStartIndex(result.getItems.size() + 1), items ++ result.getItems.map(this.toCustomMetricInfo))
      }
    }
    loop(analytics.management().customMetrics().list(accountId, webProperty), Vector.empty[GAApi.CustomMetricInfo])
  }

  private def toCustomMetricInfo(customMetric: CustomMetric): GAApi.CustomMetricInfo = {
    GAApi.CustomMetricInfo(customMetric.getAccountId,
      customMetric.getWebPropertyId,
      customMetric.getId,
      customMetric.getIndex,
      customMetric.getName,
      customMetric.getActive,
      customMetric.getCreated,
      customMetric.getUpdated
    )
  }

  def getAllCustomMetricInfo: Vector[GAApi.CustomMetricInfo] = {
    logger.info("get all custom metric info.")
    this.getAllAccountIdAndWebProperty.flatMap { case (accountId, webProperty) =>
      this.getCustomMetricInfo(accountId, webProperty)
    }
  }

  def getAllSegmentInfo(segmentType: GAApi.SegmentType.Value = GAApi.SegmentType.ALL): Vector[GAApi.SegmentInfo] = {
    logger.info(s"get all segment info. segment type: ${segmentType.toString}")
    @tailrec
    def loop(requestSegments: Analytics#Management#Segments#List, items: Vector[GAApi.SegmentInfo]): Vector[GAApi.SegmentInfo] = {
      //val result: Segments = this.apiCall(requestSegments)(_.execute())
      val result: Segments = this.call(requestSegments)(_.execute())

      if (result.getNextLink == null) {
        items ++ result.getItems
          .filter(item => this.segmentFilter(item, segmentType))
          .map(this.toSegmentInfo)
      } else {
        loop(requestSegments.setStartIndex(result.getItems.size() + 1),
          items ++ result.getItems
            .filter(item => this.segmentFilter(item, segmentType))
            .map(this.toSegmentInfo))
      }
    }
    loop(analytics.management().segments().list(), Vector.empty[GAApi.SegmentInfo])
  }

  private def toSegmentInfo(segment: Segment): GAApi.SegmentInfo = {
    GAApi.SegmentInfo(segment.getId,
      segment.getSegmentId,
      segment.getType,
      segment.getName,
      segment.getDefinition,
      segment.getCreated,
      segment.getUpdated)
  }

  private def segmentFilter(segment: Segment, segmentType: GAApi.SegmentType.Value): Boolean = {
    segmentType match {
      case GAApi.SegmentType.ALL =>
        true
      case _ =>
        segment.getType == segmentType.toString
    }
  }
}

object GAApi extends LazyLogging {
  private val applicationName = AppConfig.applicationName
  private val httpTransport = GoogleNetHttpTransport.newTrustedTransport()
  private val jsonFactory = JacksonFactory.getDefaultInstance

  def apply(analytics: Analytics): GAApi = {
    logger.info("create google analytics object")
    new GAApi(analytics)
  }

  def apply(googleCredentialJsonString: String): GAApi = {
    this.apply(this.createAnalytics(googleCredentialJsonString))
  }

  def apply(googleCredential: GoogleCredential): GAApi = {
    this.apply(this.createAnalytics(googleCredential))
  }

  private def createAnalytics(googleCredential: GoogleCredential): Analytics = {
    logger.info("create google analytics api service client.")

    new Analytics.Builder(
      httpTransport,
      jsonFactory,
      googleCredential
    ).setApplicationName(applicationName)
      .build()
  }

  private def createAnalytics(googleCredentialJsonString: String): Analytics = {
    this.createAnalytics(GoogleCredentialFactory.createAnalyticsCredential(googleCredentialJsonString))
  }

  case class AccountInfo(accountId: String, accountName: String, webPropertyId: String, internalWebPropertyId: String, webPropertyName: String, profileId: String, profileName: String)
  case class CustomDimensionInfo(accountId: String, webPropertyId: String, id: String, index: Int, name: String, active: Boolean, created: DateTime, updated: DateTime)
  case class CustomMetricInfo(accountId: String, webPropertyId: String, id: String, index: Int, name: String, active: Boolean, created: DateTime, updated: DateTime)
  /** id: segment id, segmentId: segment id for core reporting. */
  case class SegmentInfo(id: String, idForReportingAPI: String, segmentType: String, name: String, definition: String, created: DateTime, updated: DateTime)

  object SegmentType extends Enumeration { val ALL, CUSTOM, BUILT_IN = Value }
}
