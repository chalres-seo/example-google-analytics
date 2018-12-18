package com.example.utils

import scala.concurrent.{Await, Future}

object FutureUtils {
  private val defaultTimeout = AppConfig.defaultFutureTimeout

  /** throws: [[Await.result]] */
  @throws(classOf[Exception])
  def awaitResultFuture[R](future: Future[R]): R = Await.result(future, defaultTimeout)
}
