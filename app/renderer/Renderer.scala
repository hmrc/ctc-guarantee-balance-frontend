/*
 * Copyright 2022 HM Revenue & Customs
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

package renderer

import config.FrontendAppConfig
import play.api.libs.json.{JsObject, Json, OWrites}
import play.api.mvc.RequestHeader
import play.twirl.api.Html
import uk.gov.hmrc.nunjucks.NunjucksRenderer

import javax.inject.Inject
import scala.concurrent.Future

class Renderer @Inject() (appConfig: FrontendAppConfig, renderer: NunjucksRenderer) {

  def render(template: String)(implicit request: RequestHeader): Future[Html] =
    renderTemplate(template, Json.obj())

  def render[A](template: String, ctx: A)(implicit request: RequestHeader, writes: OWrites[A]): Future[Html] =
    renderTemplate(template, Json.toJsObject(ctx))

  def render(template: String, ctx: JsObject)(implicit request: RequestHeader): Future[Html] =
    renderTemplate(template, ctx)

  private def renderTemplate(template: String, ctx: JsObject)(implicit request: RequestHeader): Future[Html] =
    renderer.render(template, ctx ++ Json.obj("config" -> config))

  private val config: JsObject = Json.obj(
    "betaFeedbackUnauthenticatedUrl" -> appConfig.betaFeedbackUnauthenticatedUrl,
    "serviceIdentifier"              -> appConfig.contactFormServiceIdentifier,
    "contactHost"                    -> appConfig.contactHost,
    "signOutUrl"                     -> appConfig.signOutUrl,
    "timeoutSeconds"                 -> appConfig.timeoutSeconds,
    "countdownSeconds"               -> appConfig.countdownSeconds,
    "trackingConsentUrl"             -> appConfig.trackingConsentUrl,
    "gtmContainer"                   -> appConfig.gtmContainer,
    "userResearchUrl"                -> appConfig.userResearchUrl,
    "showPhaseBanner"                -> appConfig.showPhaseBanner,
    "showUserResearchBanner"         -> appConfig.showUserResearchBanner,
    "manageTransitMovementsUrl"      -> appConfig.manageTransitMovementsUrl,
    "nctsEnquiriesUrl"               -> appConfig.nctsEnquiriesUrl
  )
}
