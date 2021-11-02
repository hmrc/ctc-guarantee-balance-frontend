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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import javax.inject.Inject
import models.backend.{BalanceRequestNotMatched, BalanceRequestPending, BalanceRequestPendingExpired, BalanceRequestResponse, BalanceRequestSuccess}
import models.requests.DataRequest
import models.values.BalanceId
import pages.BalancePage
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import renderer.Renderer
import repositories.SessionRepository
import services.GuaranteeBalanceService
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class WaitOnGuaranteeBalanceController @Inject() (
  balanceService: GuaranteeBalanceService,
  sessionRepository: SessionRepository,
  val config: FrontendAppConfig,
  identify: IdentifierAction,
  val controllerComponents: MessagesControllerComponents,
  val renderer: Renderer,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with TechnicalDifficultiesPage {

  def onPageLoad(balanceId: BalanceId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      displayWaitPage(balanceId)
  }

  def onSubmit(balanceId: BalanceId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val response =
        balanceService.pollForGuaranteeBalance(balanceId, appConfig.guaranteeBalanceDelayInSecond seconds, appConfig.guaranteeBalanceMaxTimeInSecond seconds)
      response.flatMap(processResponse(_, displayWaitPage))
  }

  def processResponse(response: Either[HttpResponse, BalanceRequestResponse], processPending: BalanceId => Future[Result])(implicit
    request: DataRequest[_]
  ): Future[Result] =
    response match {
      case Right(BalanceRequestPending(balanceId)) =>
        processPending(balanceId)
      case Right(balanceRequest: BalanceRequestSuccess) =>
        processSuccessResponse(balanceRequest)
      case Right(BalanceRequestNotMatched) =>
        Future.successful(Redirect(routes.DetailsDontMatchController.onPageLoad()))
      case Right(BalanceRequestPendingExpired(balanceId)) =>
        Future.successful(Redirect(routes.TryGuaranteeBalanceAgainController.onPageLoad(balanceId)))
      case _ =>
        renderTechnicalDifficultiesPage
    }

  private def processSuccessResponse(balanceRequest: BalanceRequestSuccess)(implicit request: DataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(BalancePage, balanceRequest.formatForDisplay))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(routes.BalanceConfirmationController.onPageLoad())

  private def displayWaitPage(balanceId: BalanceId)(implicit request: Request[_]): Future[Result] = {
    val json = Json.obj(
      "balanceId"         -> balanceId,
      "waitTimeInSeconds" -> config.guaranteeBalanceMaxTimeInSecond
    )
    renderer.render("waitOnGuaranteeBalance.njk", json).map(Ok(_))
  }
}
