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
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, service: ServicesConfig) {

  lazy val appName: String     = configuration.get[String]("appName")
  lazy val feedbackUrl: String = configuration.get[String]("urls.feedback")

  val signOutUrl: String = configuration.get[String]("urls.logoutContinue") + configuration.get[String]("urls.feedback")

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
  lazy val replaceIndexes: Boolean           = configuration.get[Boolean]("feature-flags.replace-indexes")

  val encryptionKey: String      = configuration.get[String]("encryption.key")
  val encryptionEnabled: Boolean = configuration.get[Boolean]("encryption.enabled")

}
