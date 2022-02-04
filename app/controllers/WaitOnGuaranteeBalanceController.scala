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

package controllers

import config.FrontendAppConfig
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import handlers.GuaranteeBalanceResponseHandler
import models.values.BalanceId
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import renderer.Renderer
import services.GuaranteeBalanceService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import javax.inject.Inject
import models.UserAnswers
import models.requests.DataRequest
import pages.BalanceIdPage
import repositories.SessionRepository
import scala.concurrent.{ExecutionContext, Future}

class WaitOnGuaranteeBalanceController @Inject() (
  balanceService: GuaranteeBalanceService,
  val controllerComponents: MessagesControllerComponents,
  responseHandler: GuaranteeBalanceResponseHandler,
  config: FrontendAppConfig,
  identify: IdentifierAction,
  renderer: Renderer,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(balanceId: BalanceId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val json = Json.obj(
        "balanceId"         -> balanceId,
        "waitTimeInSeconds" -> config.guaranteeBalanceDisplayDelay
      )
      renderer.render("waitOnGuaranteeBalance.njk", json).map(Ok(_))
  }

  def checkDetails(balanceId: BalanceId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(BalanceIdPage, balanceId))
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(routes.CheckYourAnswersController.onPageLoad())
  }

  def onSubmit(balanceId: BalanceId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      for {
        _           <- removeBalanceIdFromUserAnswers
        displayPage <- pollForGuaranteeBalance(balanceId)
      } yield displayPage
  }

  private def removeBalanceIdFromUserAnswers()(implicit request: DataRequest[AnyContent]): Future[UserAnswers] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.remove(BalanceIdPage))
      _              <- sessionRepository.set(updatedAnswers)
    } yield updatedAnswers

  private def pollForGuaranteeBalance(balanceId: BalanceId)(implicit request: DataRequest[AnyContent]): Future[Result] =
    balanceService
      .pollForGuaranteeBalance(balanceId = balanceId)
      .flatMap(responseHandler.processResponse(_))

}
