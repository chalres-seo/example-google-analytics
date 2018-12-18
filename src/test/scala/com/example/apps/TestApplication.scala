package com.example.apps

import com.typesafe.scalalogging.LazyLogging
import org.junit.Test

class TestApplication extends LazyLogging {

  @Test
  def testApplication(): Unit = {
    logger.debug("test application start.")
    Application.main(Array(""))
  }
}
