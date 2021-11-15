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
  BalanceRequestSuccess,
  BalanceRequestUnsupportedGuaranteeType
}
import models.requests.DataRequest
import models.values.BalanceId
import pages.BalancePage
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc._
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.http.HttpResponse
import play.api.http.Status.TOO_MANY_REQUESTS

import scala.concurrent.{ExecutionContext, Future}

class GuaranteeBalanceResponseHandler @Inject() (
  sessionRepository: SessionRepository,
  renderer: Renderer,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends Logging {

  def processResponse(response: Either[HttpResponse, BalanceRequestResponse], processPending: BalanceId => Future[Result])(implicit
    request: DataRequest[_]
  ): Future[Result] =
    response match {
      case Right(balanceRequestResponse) => processBalanceRequestResponse(balanceRequestResponse, processPending)
      case Left(failureResponse)         => processHttpResponse(failureResponse)
    }

  def processBalanceRequestResponse(response: BalanceRequestResponse, processPending: BalanceId => Future[Result])(implicit
    request: DataRequest[_]
  ): Future[Result] =
    response match {
      case BalanceRequestPending(balanceId) =>
        processPending(balanceId)
      case successResponse: BalanceRequestSuccess =>
        processSuccessResponse(successResponse)
      case BalanceRequestNotMatched =>
        Future.successful(Redirect(controllers.routes.DetailsDontMatchController.onPageLoad()))
      case BalanceRequestPendingExpired(_) =>
        Future.successful(Redirect(controllers.routes.TryGuaranteeBalanceAgainController.onPageLoad()))
      case BalanceRequestUnsupportedGuaranteeType =>
        Future.successful(Redirect(controllers.routes.UnsupportedGuaranteeTypeController.onPageLoad()))
      case fe: BalanceRequestFunctionalError =>
        logger.warn(s"[GuaranteeBalanceResponseHandler][processBalanceRequestResponse]Failed to process Response: ${fe.errors}")
        technicalDifficulties()
    }

  private def processHttpResponse(response: HttpResponse)(implicit request: DataRequest[_]): Future[Result] =
    response match {
      case failureResponse if failureResponse.status.equals(TOO_MANY_REQUESTS) =>
        Future.successful(Redirect(controllers.routes.RateLimitController.onPageLoad()))
      case failureResponse =>
        logger.warn(s"[GuaranteeBalanceResponseHandler][processHttpResponse]Failed to process Response: $failureResponse")
        technicalDifficulties()
    }

  private def processSuccessResponse(balanceResponse: BalanceRequestSuccess)(implicit request: DataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(BalancePage, balanceResponse.formatForDisplay))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(controllers.routes.BalanceConfirmationController.onPageLoad())

  private def technicalDifficulties()(implicit request: Request[_]): Future[Result] = {
    val json = Json.obj(
      "contactUrl" -> appConfig.nctsEnquiriesUrl
    )
    renderer.render("technicalDifficulties.njk", json).map(InternalServerError(_))
  }
}
