package com.example.utils

import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.duration.Duration

object AppConfig extends LazyLogging {
  /** read custom application.conf */
//  private val conf: Config = this.readConfigFromFile("conf/application.conf")
  private val conf: Config = ConfigFactory.load().resolve()

  /** application config */
  val applicationName: String = conf.getString("application.name")
  val defaultWaitMillis = 3000L
  val savePathRoot: String = conf.getString("application.savePathRoot")

  /** future config */
  val defaultFutureTimeout: Duration = Duration.apply(conf.getLong("application.futureTimeWait"), TimeUnit.MILLISECONDS)

  /** google analytics config */
  val defaultGoogleAnalyticsReportFetchSize: Int = conf.getInt("application.googleAnalyticsReportFetchSize")
}