package com.example.service.google.analytics

import com.example.utils.JsonUtils
import com.google.api.services.analyticsreporting.v4.model.Report
import com.typesafe.scalalogging.LazyLogging

import scala.collection.JavaConversions._

object GAReportingHandler extends LazyLogging {
  type GAReportingHandlerType = (Report, String) => Boolean

  def stdoutReportingData: GAReportingHandlerType = { case (report, pageToken) =>
    logger.debug("stdout reporting data.")
    try {
      this.getReportingData(report).foreach(println)
      true
    } catch {
      case t: Throwable =>
        this.failedReportingHandlerLog(pageToken, t.getMessage)
        false
    }
  }

  def stdoutCsvReportingData: GAReportingHandlerType = { case (report, pageToken) =>
    logger.debug("stdout csv form reporting data.")
    try {
      this.getCsvReportingData(report).foreach { csvData =>
        println(s"header: ${csvData._1}, body: ${csvData._2}")
      }

      true
    } catch {
      case t: Throwable =>
        this.failedReportingHandlerLog(pageToken, t.getMessage)
        false
    }
  }

  def stdoutJsonReportingData: GAReportingHandlerType = { case (report, pageToken) =>
    logger.debug("stdout json form reporting data.")
    try {
      this.getJsonReportingData(report).foreach(println)
      true
    } catch {
      case t: Throwable =>
        this.failedReportingHandlerLog(pageToken, t.getMessage)
        false
    }
  }

  private def failedReportingHandlerLog(pageToken: String, msg: String): Unit = {
    logger.error(s"failed google analytics reporting handler. page token: $pageToken, msg: $msg")
  }

  private def getReportingData(report: Report): Vector[Map[String, String]] = {
    logger.debug("get reporting data.")
    if (this.nonEmptyReportData(report)) {
      val header: Vector[String] = this.getHeader(report)
      val rows: Vector[Vector[String]] = this.getRows(report)

      rows.map(header.zip(_).toMap)
    } else {
      Vector.empty[Map[String, String]]
    }
  }

  private def getJsonReportingData(report: Report): Vector[String] = {
    logger.debug("get json form reporting data.")
    val reportingData = this.getReportingData(report)

    if (reportingData.nonEmpty) {
      reportingData.map(JsonUtils.mapToJsonString)
    } else Vector.empty[String]
  }

  private def getCsvReportingData(report: Report): Vector[(String, String)] = {
    logger.debug("get csv form reporting data.")
    val reportingData: Vector[Map[String, String]] = this.getReportingData(report)

    if (reportingData.nonEmpty) {
      reportingData.map(m => (m.keySet.mkString(","), m.values.mkString(",")))
    } else Vector.empty[(String, String)]
  }

  private def isEmptyReportData(report: Report): Boolean = {
    if (report.getData.isEmpty) {
      logger.info("report data is non empty.")
      true
    } else {
      logger.info("report data is empty.")
      false
    }
  }

  private def nonEmptyReportData(report: Report): Boolean = {
    !this.isEmptyReportData(report)
  }

  private def getHeader(report: Report): Vector[String] = {
    logger.debug("get report header.")
    this.getMetricHeader(report) ++ this.getDimensionHeader(report)
  }

  private def getRows(report: Report): Vector[Vector[String]] = {
    logger.debug("get report rows.")
    val metricValues = this.getMetricValues(report)
    val dimensionValues = this.getDimensionValues(report)

    (metricValues.nonEmpty, dimensionValues.nonEmpty) match {
      case (true, true) => metricValues.zip(dimensionValues).map(v => v._1 ++ v._2)
      case (true, false) => metricValues
      case (false, true) => dimensionValues
      case (false, false) => Vector.empty[Vector[String]]
    }
  }

  private def getMetricHeader(report: Report): Vector[String] = {
    logger.debug("get report metric header.")
    try {
      report
        .getColumnHeader
        .getMetricHeader
        .getMetricHeaderEntries
        .map(_.getName)
        .toVector
    } catch {
      case e: NullPointerException =>
        logger.error(s"metric header is null. msg: ${e.getMessage}")
        Vector.empty[String]
      case t: Throwable =>
        logger.error("failed get metric header.")
        logger.error(t.getMessage, t)
        Vector.empty[String]
    }
  }

  private def getDimensionHeader(report: Report): Vector[String] = {
    logger.debug("get report dimension header.")
    try {
      report.getColumnHeader.getDimensions.toVector
    } catch {
      case e: NullPointerException =>
        logger.error(s"dimension header is null. msg: ${e.getMessage}")
        Vector.empty[String]
      case t: Throwable =>
        logger.error("failed get dimension header.")
        logger.error(t.getMessage, t)
        Vector.empty[String]
    }
  }

  private def getMetricValues(report: Report): Vector[Vector[String]] = {
    logger.debug("get report metric values.")
    try {
      report
        .getData
        .getRows.map(r => r.getMetrics.flatMap(_.getValues).toVector)
        .toVector
    } catch {
      case e: NullPointerException =>
        logger.info(s"report metrics row data is null. msg: ${e.getMessage}")
        Vector.empty[Vector[String]]
      case t: Throwable =>
        logger.error("failed get metrics report row data.")
        logger.error(t.getMessage, t)
        Vector.empty[Vector[String]]
    }
  }

  private def getDimensionValues(report: Report): Vector[Vector[String]] = {
    logger.debug("get report dimension values.")
    try {
      report
        .getData
        .getRows.map(r => r.getDimensions.toVector)
        .toVector
    } catch {
      case e: NullPointerException =>
        logger.info(s"report dimension row data is null. msg: ${e.getMessage}")
        Vector.empty[Vector[String]]
      case t: Throwable =>
        logger.error("failed get report dimension row data.")
        logger.error(t.getMessage, t)
        Vector.empty[Vector[String]]
    }
  }
}
