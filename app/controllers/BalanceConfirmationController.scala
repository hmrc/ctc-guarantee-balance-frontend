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
import models.NormalMode
import pages.BalancePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

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
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(BalancePage) match {
        case Some(balance) =>
          val json = Json.obj(
            "balance"                         -> balance,
            "isEnrolled"                      -> request.isEnrolled,
            "checkAnotherGuaranteeBalanceUrl" -> routes.BalanceConfirmationController.checkAnotherGuaranteeBalance().url
          )

          renderer.render("balanceConfirmation.njk", json).map(Ok(_))
        case _ =>
          logger.warn("[BalanceConfirmationController][onPageLoad] Insufficient data in user answers. Redirecting to start of guarantee balance journey.")
          Future.successful(Redirect(routes.StartController.start()))
      }
  }

  def checkAnotherGuaranteeBalance: Action[AnyContent] =
    clearUserAnswersAndRedirect(routes.EoriNumberController.onPageLoad(NormalMode).url)

  def manageTransitMovements: Action[AnyContent] =
    clearUserAnswersAndRedirect(appConfig.manageTransitMovementsUrl)

  private def clearUserAnswersAndRedirect(url: String): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sessionRepository.set(request.userAnswers.clear) map {
        _ =>
          Redirect(url)
      }
  }
}
