package com.example.utils

import java.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.api.client.json.jackson2.JacksonFactory
import com.typesafe.scalalogging.LazyLogging

import scala.collection.mutable

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

import scala.util.{Failure, Success, Try}

object JsonUtils extends LazyLogging {
  private val jacksonFactory = JacksonFactory.getDefaultInstance
  private val mapClass: Class[_ <: util.HashMap[String, String]] = new util.HashMap[String, String]().getClass
  private val jasonMapper = new ObjectMapper()

  def jsonStringToMap(jsonString: String): mutable.Map[String, String] = {
    logger.debug(s"json string to map. string: $jsonString")

    Try(jacksonFactory.createJsonParser(jsonString).parse(mapClass).asScala) match {
      case Success(result) => result
      case Failure(exception) =>
        logger.error(exception.getMessage, exception)
        mutable.Map.empty[String, String]
    }
  }

  def mapToJsonString[K, V](map: Map[K, V]): String = {
    jasonMapper.writeValueAsString(map.asJava)
  }
}
