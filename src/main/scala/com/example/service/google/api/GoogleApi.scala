package com.example.service.google.api

import java.io.IOException

import com.google.api.services.analytics.Analytics
import com.google.api.services.analyticsreporting.v4.AnalyticsReporting
import com.typesafe.scalalogging.LazyLogging

trait GoogleApi extends LazyLogging {
  def call[A, B](request: A)(fn: A => B): B = {
    //val googleApiName = this.getRequestApiName(request)
    val googleApiName = request.getClass.getTypeName

    try {
      logger.info(s"google api call. name: $googleApiName")
      fn(request)
    } catch {
      case e:Exception =>
        logger.error(s"failed google api call. name: $googleApiName, msg: ${e.getMessage}")
        throw e
    }
  }

//  private def getRequestApiName[A](request: A): String = {
//    request match {
//      case _: AnalyticsReporting#Reports#BatchGet =>
//        "analyticsreporting.reports.batchGet"
//      case _: Analytics#Management#AccountSummaries#List =>
//        "analytics.management.accountSummaries.list"
//      case _: Analytics#Management#CustomDimensions#List =>
//        logger.info("google api call. name: analytics.management.customDimensions.list.")
//        "analytics.management.customDimensions.list"
//      case _: Analytics#Management#CustomMetrics#List =>
//        logger.info("google api call. name: analytics.management.customMetrics.list.")
//        "analytics.management.customMetrics.list"
//      case _: Analytics#Management#Segments#List =>
//        logger.info("google api call. name: analytics.management.segments.list.")
//        "analytics.management.segments.list"
//      case _ =>
//        logger.info(s"google api call. api: unknown api. request class: ${request.getClass.getTypeName}")
//        "unknown api"
//    }
//  }
}
