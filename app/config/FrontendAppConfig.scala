/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, service: ServicesConfig) {

  lazy val appName: String = configuration.get[String]("appName")

  lazy val contactHost: String = configuration.get[String]("contact-frontend.host")

  val contactFormServiceIdentifier = "CTCTraders"

  val signOutUrl: String = configuration.get[String]("urls.logoutContinue") + configuration.get[String]("urls.feedback")

  private val host: String = configuration.get[String]("host")

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  val showPhaseBanner: Boolean        = configuration.get[Boolean]("banners.showPhase")
  val userResearchUrl: String         = configuration.get[String]("urls.userResearch")
  val showUserResearchBanner: Boolean = configuration.get[Boolean]("banners.showUserResearch")

  lazy val timeoutSeconds: Int   = configuration.get[Int]("session.timeoutSeconds")
  lazy val countdownSeconds: Int = configuration.get[Int]("session.countdownSeconds")
  lazy val mongoDbTtl: Int       = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  lazy val rateLimitDuration: Int               = configuration.get[Int]("rateLimit.duration")
  lazy val guaranteeBalanceDelayInSecond: Int   = configuration.get[Int]("guaranteeBalanceLookup.waitTimeInSeconds")
  lazy val guaranteeBalanceMaxTimeInSecond: Int = configuration.get[Int]("guaranteeBalanceLookup.maxTimeInSeconds")
  lazy val guaranteeBalanceExpiryTime: Int      = configuration.get[Int]("guaranteeBalanceLookup.expiryTimeInSeconds")

  lazy val languageTranslationEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.welsh-translation")

  lazy val authUrl: String          = service.baseUrl("auth")
  lazy val loginUrl: String         = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")

  lazy val guaranteeBalanceUrl: String       = service.baseUrl("common-transit-convention-guarantee-balance")
  lazy val nctsUrl: String                   = configuration.get[String]("urls.ncts")
  lazy val nctsEnquiriesUrl: String          = configuration.get[String]("urls.nctsEnquiries")
  lazy val manageTransitMovementsUrl: String = configuration.get[String]("urls.manageTransitMovements")

  lazy val guaranteeBalanceApiV2: Boolean = configuration
    .getOptional[String](
      "guaranteeBalanceApi.version"
    )
    .getOrElse("1.0")
    .equals("2.0")

}
