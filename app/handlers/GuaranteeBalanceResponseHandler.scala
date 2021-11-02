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

package handlers

import config.FrontendAppConfig
import javax.inject.Inject
import models.backend.{
  BalanceRequestFunctionalError,
  BalanceRequestNotMatched,
  BalanceRequestPending,
  BalanceRequestPendingExpired,
  BalanceRequestResponse,
  BalanceRequestSuccess
}
import models.requests.DataRequest
import models.values.BalanceId
import pages.BalancePage
import play.api.libs.json.Json
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc._
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.{ExecutionContext, Future}

class GuaranteeBalanceResponseHandler @Inject() (
  sessionRepository: SessionRepository,
  renderer: Renderer,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext) {

  def processResponse(response: Either[HttpResponse, BalanceRequestResponse], processPending: BalanceId => Future[Result])(implicit
    request: DataRequest[_]
  ): Future[Result] =
    response match {
      case Right(BalanceRequestPending(balanceId)) =>
        processPending(balanceId)
      case Right(successResponse: BalanceRequestSuccess) =>
        processSuccessResponse(successResponse)
      case Right(BalanceRequestNotMatched) =>
        Future.successful(Redirect(controllers.routes.DetailsDontMatchController.onPageLoad()))
      case Right(BalanceRequestPendingExpired(balanceId)) =>
        Future.successful(Redirect(controllers.routes.TryGuaranteeBalanceAgainController.onPageLoad(balanceId)))
      case Right(BalanceRequestFunctionalError(errors)) =>
        technicalDifficulties()
      case Left(_) =>
        technicalDifficulties()
    }

  private def processSuccessResponse(balanceRequest: BalanceRequestSuccess)(implicit request: DataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(BalancePage, balanceRequest.formatForDisplay))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(controllers.routes.BalanceConfirmationController.onPageLoad())

  private def technicalDifficulties()(implicit request: Request[_]): Future[Result] = {
    val json = Json.obj(
      "contactUrl" -> appConfig.nctsEnquiriesUrl
    )
    renderer.render("technicalDifficulties.njk", json).map(InternalServerError(_))
  }
}
