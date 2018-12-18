package com.example.apps

import com.example.service.google.analytics.{GAApi, GAReportingApi, GAReportingHandler}
import com.typesafe.scalalogging.LazyLogging
import org.joda.time.DateTime

import scala.io.Source

object Application extends LazyLogging {
  private val testGoogleCredentialJsonString = Source.fromFile("example_data/example_google_credential.json").mkString
  private val gaApi = GAApi(testGoogleCredentialJsonString)
  private val gaReporting = GAReportingApi(testGoogleCredentialJsonString)

  def main(args: Array[String]): Unit = {
    logger.info("start application.")

    logger.info("example ga api.")
    this.exampleGAApi()

    logger.info("example ga reporting")
    this.exampleGAReporting

    logger.info("example ga query")
    this.exampleGAQuery

  }

  def exampleGAApi() = {
    val accountSummaries = gaApi.getAccountSummaries
    val accountInfoList = gaApi.getAllAccountInfo
    val allCustomDimensionInfo = gaApi.getAllCustomDimensionInfo
    val allCustomMetricInfo = gaApi.getAllCustomMetricInfo
    val allSegmentInfo = gaApi.getAllSegmentInfo()
    val allCustomSegmentInfo = gaApi.getAllSegmentInfo(GAApi.SegmentType.CUSTOM)
    val allBuiltInSegmentInfo = gaApi.getAllSegmentInfo(GAApi.SegmentType.BUILT_IN)

    logger.info("account summaries.")
    accountSummaries.foreach(println)

    logger.info("account info list.")
    accountInfoList.foreach(println)

    logger.info("custom dimension info.")
    allCustomDimensionInfo.foreach(println)

    logger.info("custom metric info.")
    allCustomMetricInfo.foreach(println)

    logger.info("segment info.")
    allSegmentInfo.foreach(println)

    logger.info("custom segment info.")
    allCustomSegmentInfo.foreach(println)

    logger.info("built inf segment info.")
    allBuiltInSegmentInfo.foreach(println)
  }

  def exampleGAReporting = {
    val testStartDate = DateTime.now.minusDays(14).toString("yyyy-MM-dd")
    val testEndDate = DateTime.now.minusDays(1).toString("yyyy-MM-dd")
    val testIds = "ga:59640368"
    val testMetrics: Vector[String] = "ga:users,ga:sessions".split(",").toVector
    val testDimensions: Option[Vector[String]] = Option("ga:segment,ga:date,ga:dimension1".split(",").toVector)

    val testSegmentId = Option("gaid::gQtkmiw9T4q0xZUn04hIJg")
    val testSegmentExpr: Option[String] = Option("users::condition::ga:sessions>=0")

    logger.info("segment id.")
    gaReporting
      .reportRequest(testIds,
        testStartDate,
        testEndDate,
        testMetrics,
        testDimensions,
        testSegmentId)(GAReportingHandler.stdoutJsonReportingData)

    logger.info("segment expr.")
    gaReporting
      .reportRequest(testIds,
        testStartDate,
        testEndDate,
        testMetrics,
        testDimensions,
        testSegmentExpr)(GAReportingHandler.stdoutCsvReportingData)

  }

  def exampleGAQuery = {
    val testStartDate = DateTime.now.minusDays(14).toString("yyyy-MM-dd")
    val testEndDate = DateTime.now.minusDays(1).toString("yyyy-MM-dd")

    val testGAJsonQueryStringPath = "example_data/example_google_analytics_query.json"
    val testGAJsonQueryString = Source.fromFile(testGAJsonQueryStringPath)
      .mkString
      .replace("start-date", testStartDate)
      .replace("end-date", testEndDate)

    logger.info("ga query")
    logger.info(testGAJsonQueryString)
    gaReporting.reportRequest(testGAJsonQueryString)(GAReportingHandler.stdoutReportingData)
  }
}
