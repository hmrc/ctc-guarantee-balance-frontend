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
import models.backend._
import models.requests.DataRequest
import models.values.BalanceId
import pages.{AccessCodePage, BalancePage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.Logging
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.mvc.Results.{InternalServerError, Redirect}
import play.api.mvc._
import renderer.Renderer
import repositories.SessionRepository
import services.AuditService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import viewModels.audit.{SuccessfulBalanceAuditModel, UnsuccessfulBalanceAuditModel}

import scala.concurrent.{ExecutionContext, Future}

class GuaranteeBalanceResponseHandler @Inject() (
  sessionRepository: SessionRepository,
  renderer: Renderer,
  appConfig: FrontendAppConfig,
  auditService: AuditService
)(implicit ec: ExecutionContext)
    extends Logging {

  def processResponse(response: Either[HttpResponse, BalanceRequestResponse], processPending: BalanceId => Future[Result])(implicit
    hc: HeaderCarrier,
    request: DataRequest[_]
  ): Future[Result] =
    response match {
      case Right(balanceRequestResponse) => processBalanceRequestResponse(balanceRequestResponse, processPending)
      case Left(failureResponse)         => processHttpResponse(failureResponse)
    }

  def processBalanceRequestResponse(response: BalanceRequestResponse, processPending: BalanceId => Future[Result])(implicit
    hc: HeaderCarrier,
    request: DataRequest[_]
  ): Future[Result] =
    response match {
      case BalanceRequestPending(balanceId) =>
        processPending(balanceId)
      case successResponse: BalanceRequestSuccess =>
        auditService.audit(
          SuccessfulBalanceAuditModel.build(
            request.userAnswers.get(EoriNumberPage).getOrElse("-").toString,
            request.userAnswers.get(GuaranteeReferenceNumberPage).getOrElse("-").toString,
            request.userAnswers.get(AccessCodePage).getOrElse("-").toString,
            OK,
            successResponse.currency.toString.trim + " " + successResponse.balance.toString.trim
          )
        )
        processSuccessResponse(successResponse)

      case BalanceRequestNotMatched =>
        auditService.audit(
          UnsuccessfulBalanceAuditModel.build(
            "Balance Request Not Matched",
            "Balance Request Not Matched Audit",
            request.userAnswers.get(EoriNumberPage).getOrElse("-"),
            request.userAnswers.get(GuaranteeReferenceNumberPage).getOrElse("-"),
            request.userAnswers.get(AccessCodePage).getOrElse("-"),
            SEE_OTHER,
            "Balance Request Details Do Not Match - But which ?????"
          )
        )

        Future.successful(Redirect(controllers.routes.DetailsDontMatchController.onPageLoad()))

      case BalanceRequestPendingExpired(_) =>
        auditService.audit(
          UnsuccessfulBalanceAuditModel.build(
            "Balance Request Pending Expired",
            "Balance Request Pending Expired Audit",
            request.userAnswers.get(EoriNumberPage).getOrElse("-"),
            request.userAnswers.get(GuaranteeReferenceNumberPage).getOrElse("-"),
            request.userAnswers.get(AccessCodePage).getOrElse("-"),
            SEE_OTHER,
            "Balance Request Pending Expired"
          )
        )

        Future.successful(Redirect(controllers.routes.TryGuaranteeBalanceAgainController.onPageLoad()))
      case BalanceRequestUnsupportedGuaranteeType =>
        auditService.audit(
          UnsuccessfulBalanceAuditModel.build(
            "Balance Request Unsupported Guarantee Type",
            "Balance Request Unsupported Guarantee Type Audit",
            request.userAnswers.get(EoriNumberPage).getOrElse("-"),
            request.userAnswers.get(GuaranteeReferenceNumberPage).getOrElse("-"),
            request.userAnswers.get(AccessCodePage).getOrElse("-"),
            SEE_OTHER,
            "Balance Request Unsupported Guarantee Type Type"
          )
        )

        Future.successful(Redirect(controllers.routes.UnsupportedGuaranteeTypeController.onPageLoad()))
      case fe: BalanceRequestFunctionalError =>
        auditService.audit(
          UnsuccessfulBalanceAuditModel.build(
            "Balance Request Functional Error",
            "Balance Request Functional Error Audit",
            request.userAnswers.get(EoriNumberPage).getOrElse("-"),
            request.userAnswers.get(GuaranteeReferenceNumberPage).getOrElse("-"),
            request.userAnswers.get(AccessCodePage).getOrElse("-"),
            INTERNAL_SERVER_ERROR,
            s"Failed to process Response: ${fe.errors}"
          )
        )
        logger.warn(s"[GuaranteeBalanceResponseHandler][processBalanceRequestResponse]Failed to process Response: ${fe.errors}")
        technicalDifficulties()
    }

  private def processHttpResponse(response: HttpResponse)(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Result] =
    response match {
      case failureResponse if failureResponse.status.equals(TOO_MANY_REQUESTS) =>
        auditService.audit(
          UnsuccessfulBalanceAuditModel.build(
            "Rate Limit",
            "Rate Limit Audit",
            request.userAnswers.get(EoriNumberPage).getOrElse("-"),
            request.userAnswers.get(GuaranteeReferenceNumberPage).getOrElse("-"),
            request.userAnswers.get(AccessCodePage).getOrElse("-"),
            TOO_MANY_REQUESTS,
            "Rate Limit Exceeded"
          )
        )
        Future.successful(Redirect(controllers.routes.RateLimitController.onPageLoad()))
      case failureResponse =>
        logger.warn(s"[GuaranteeBalanceResponseHandler][processHttpResponse]Failed to process Response: $failureResponse")

        auditService.audit(
          UnsuccessfulBalanceAuditModel.build(
            "Rate Limit",
            "Rate Limit Audit",
            request.userAnswers.get(EoriNumberPage).getOrElse("-"),
            request.userAnswers.get(GuaranteeReferenceNumberPage).getOrElse("-"),
            request.userAnswers.get(AccessCodePage).getOrElse("-"),
            INTERNAL_SERVER_ERROR,
            s"Failed to process Response: $failureResponse"
          )
        )

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
