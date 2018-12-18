package com.example.utils

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths, StandardOpenOption}

import com.typesafe.scalalogging.LazyLogging

import scala.util.{Failure, Success, Try}

object FileUtils extends LazyLogging {
  @throws(classOf[IOException])
  @throws(classOf[IllegalArgumentException])
  def writeFile(savePathString: String, record: String, append: Boolean): Unit = {
    logger.info(s"write file. file path: $savePathString, append: $append")
    val savePath = Paths.get(savePathString)

    if (Files.notExists(savePath.getParent)) {
      Files.createDirectories(savePath.getParent)
    }
    require(Files.exists(savePath.getParent), s"save dir is not exist. file path: $savePath")

    val fileWriter = if (append) {
      Files.newBufferedWriter(savePath,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND)
    } else {
      Files.newBufferedWriter(savePath,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE)
    }

    Try {
      fileWriter.write(record)
    } match {
      case Success(_) =>
        logger.debug(s"write record: $record")
      case Failure(e) =>
        logger.error(e.getMessage)
        logger.error(record)
    }

    logger.debug("file flushing and close.")
    fileWriter.flush()
    fileWriter.close()
  }

  @throws(classOf[IOException])
  @throws(classOf[IllegalArgumentException])
  def writeFile(savePathString: String, records: Vector[String], append: Boolean): Unit = {
    logger.info(s"write file. file path: $savePathString, append: $append")
    val savePath = Paths.get(savePathString)

    if (Files.notExists(savePath.getParent)) {
      Files.createDirectories(savePath.getParent)
    }
    require(Files.exists(savePath.getParent), s"save dir is not exist. file path: $savePath")

    val fileWriter = if (append) {
      Files.newBufferedWriter(savePath,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND)
    } else {
      Files.newBufferedWriter(savePath,
        StandardCharsets.UTF_8,
        StandardOpenOption.CREATE)
    }

    records.foreach { record =>
      Try {
        fileWriter.write(record)
        fileWriter.newLine()
      } match {
        case Success(_) =>
          logger.debug(s"write record: $record")
        case Failure(e) =>
          logger.error(e.getMessage)
          logger.error(record)
      }
    }

    logger.debug("file flushing and close.")
    fileWriter.flush()
    fileWriter.close()
  }

  @throws(classOf[IOException])
  @throws(classOf[IllegalArgumentException])
  def writeFileIfNotExist(savePathString: String, record: String): Unit = {
    if (Files.notExists(Paths.get(savePathString))) {
      this.writeFile(savePathString, record, append = false)
    }
  }
}
