package com.example.service.google.credential

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.services.analytics.AnalyticsScopes
import com.google.api.services.analyticsreporting.v4.AnalyticsReportingScopes
import com.typesafe.scalalogging.LazyLogging

object GoogleCredentialFactory extends LazyLogging {
  def createAnalyticsCredential(googleCredentialJsonString: String): GoogleCredential = {
    this.createGoogleCredential(googleCredentialJsonString)(str =>
      GoogleCredential
        .fromStream(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)))
        .createScoped(AnalyticsScopes.all())
    )
  }

  def createAnalyticsReportingCredential(googleCredentialJsonString: String): GoogleCredential = {
    this.createGoogleCredential(googleCredentialJsonString)(str =>
      GoogleCredential
        .fromStream(new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8)))
        .createScoped(AnalyticsReportingScopes.all()))
  }

  private def createGoogleCredential(googleCredentialString: String)(fn: String => GoogleCredential): GoogleCredential = {
    val googleCredential = fn(googleCredentialString)

    logger.info(s"create google analytics credential.\n" +
      s"\t|service account id: ${googleCredential.getServiceAccountId}\n" +
      s"\t|service account user: ${googleCredential.getServiceAccountUser}\n" +
      s"\t|project id: ${googleCredential.getServiceAccountProjectId}\n" +
      s"\t|scope: ${googleCredential.getServiceAccountScopesAsString}")

    googleCredential
  }
}