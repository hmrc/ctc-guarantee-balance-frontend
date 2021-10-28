/*
 * Copyright 2021 HM Revenue & Customs
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

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  lazy val appName = configuration.get[String]("appName")

  lazy val contactHost: String        = configuration.get[Service]("microservice.services.contact-frontend").baseUrl
  lazy val contactFrontendUrl: String = configuration.get[Service]("microservice.services.contact-frontend").fullServiceUrl
  val contactFormServiceIdentifier    = "CTCTraders"

  val trackingConsentUrl: String = configuration.get[String]("microservice.services.tracking-consent-frontend.url")
  val gtmContainer: String       = configuration.get[String]("microservice.services.tracking-consent-frontend.gtm.container")

  val signOutUrl: String = configuration.get[String]("urls.logoutContinue") + configuration.get[String]("urls.feedback")

  val betaFeedbackUrl                = s"$contactFrontendUrl/beta-feedback"
  val betaFeedbackUnauthenticatedUrl = s"$contactFrontendUrl/beta-feedback-unauthenticated"

  val showPhaseBanner: Boolean        = configuration.get[Boolean]("banners.showPhase")
  val userResearchUrl: String         = configuration.get[String]("urls.userResearch")
  val showUserResearchBanner: Boolean = configuration.get[Boolean]("banners.showUserResearch")

  lazy val timeoutSeconds: String   = configuration.get[String]("session.timeoutSeconds")
  lazy val countdownSeconds: String = configuration.get[String]("session.countdownSeconds")
  lazy val rateLimitDuration: Int   = configuration.get[Int]("rateLimit.duration")

  lazy val languageTranslationEnabled: Boolean = configuration.get[Boolean]("microservice.services.features.welsh-translation")

  lazy val authUrl: String          = configuration.get[Service]("auth").baseUrl
  lazy val loginUrl: String         = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")

  lazy val nctsEnquiriesUrl: String = configuration.get[String]("urls.nctsEnquiries")
}
