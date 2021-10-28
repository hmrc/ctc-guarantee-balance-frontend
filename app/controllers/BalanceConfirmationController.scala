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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import models.requests.DataRequest
import models.{Balance, NormalMode}
import pages.IsNctsUserPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class BalanceConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val json = Json.obj(
        "balance"                         -> Balance(8500).toString, // TODO - retrieve actual balance
        "isNctsUser"                      -> isNctsUser,
        "checkAnotherGuaranteeBalanceUrl" -> routes.BalanceConfirmationController.checkAnotherGuaranteeBalance().url
      )

      renderer.render("balanceConfirmation.njk", json).map(Ok(_))
  }

  def checkAnotherGuaranteeBalance: Action[AnyContent] =
    clearUserAnswersAndRedirect(
      isNctsUser => routes.EoriNumberController.onPageLoad(NormalMode, isNctsUser).url
    )

  def manageTransitMovements: Action[AnyContent] =
    clearUserAnswersAndRedirect(
      _ => appConfig.manageTransitMovementsUrl
    )

  private def clearUserAnswersAndRedirect(url: Boolean => String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sessionRepository.set(request.userAnswers.clear) map {
        _ =>
          Redirect(url(isNctsUser))
      }
  }

  private def isNctsUser(implicit request: DataRequest[AnyContent]): Boolean =
    request.userAnswers.get(IsNctsUserPage).getOrElse(false)
}
