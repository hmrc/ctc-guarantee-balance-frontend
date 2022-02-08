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

package handlers

import config.FrontendAppConfig
import javax.inject.Inject
import models.backend._
import models.requests.DataRequest
import org.joda.time.LocalDateTime
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
import viewModels.audit.AuditConstants._
import viewModels.audit.{ErrorMessage, SuccessfulBalanceAuditModel, UnsuccessfulBalanceAuditModel}

import scala.concurrent.{ExecutionContext, Future}

class GuaranteeBalanceResponseHandler @Inject() (
  sessionRepository: SessionRepository,
  renderer: Renderer,
  appConfig: FrontendAppConfig,
  auditService: AuditService
)(implicit ec: ExecutionContext)
    extends Logging {

  def processResponse(response: Either[HttpResponse, BalanceRequestResponse])(implicit
    hc: HeaderCarrier,
    request: DataRequest[_]
  ): Future[Result] =
    response match {
      case Right(balanceRequestResponse) => processBalanceRequestResponse(balanceRequestResponse)
      case Left(failureResponse)         => processHttpResponse(failureResponse)
    }

  private def processBalanceRequestResponse(response: BalanceRequestResponse)(implicit
    hc: HeaderCarrier,
    request: DataRequest[_]
  ): Future[Result] =
    response match {
      case BalanceRequestPending(balanceId) =>
        Future.successful(Redirect(controllers.routes.WaitOnGuaranteeBalanceController.onPageLoad(balanceId)))

      case successResponse: BalanceRequestSuccess =>
        auditSuccess(successResponse)
        processSuccessResponse(successResponse)

      case _: BalanceRequestSessionExpired =>
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))

      case BalanceRequestNotMatched(errorPointer) =>
        auditBalanceRequestNotMatched(errorPointer)
        Future.successful(Redirect(controllers.routes.DetailsDontMatchController.onPageLoad()))

      case _: BalanceRequestRateLimit =>
        auditRateLimit
        Future.successful(Redirect(controllers.routes.RateLimitController.onPageLoad()))

      case BalanceRequestPendingExpired(_) =>
        auditError(
          SEE_OTHER,
          ErrorMessage(AUDIT_ERROR_REQUEST_EXPIRED, AUDIT_DEST_TRY_AGAIN)
        )
        Future.successful(Redirect(controllers.routes.TryGuaranteeBalanceAgainController.onPageLoad()))

      case BalanceRequestUnsupportedGuaranteeType =>
        auditError(
          SEE_OTHER,
          ErrorMessage(AUDIT_ERROR_UNSUPPORTED_TYPE, AUDIT_DEST_UNSUPPORTED_TYPE)
        )

        Future.successful(Redirect(controllers.routes.UnsupportedGuaranteeTypeController.onPageLoad()))
      case fe: BalanceRequestFunctionalError =>
        auditError(
          INTERNAL_SERVER_ERROR,
          ErrorMessage(s"Failed to process Response: ${fe.errors}", AUDIT_DEST_TECHNICAL_DIFFICULTIES)
        )
        technicalDifficulties()
    }

  private def processHttpResponse(response: HttpResponse)(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Result] =
    response match {
      case failureResponse if failureResponse.status.equals(TOO_MANY_REQUESTS) =>
        auditError(
          TOO_MANY_REQUESTS,
          ErrorMessage(AUDIT_ERROR_RATE_LIMIT_EXCEEDED, AUDIT_DEST_RATE_LIMITED)
        )
        Future.successful(Redirect(controllers.routes.RateLimitController.onPageLoad()))
      case failureResponse =>
        logger.warn(s"[GuaranteeBalanceResponseHandler][processHttpResponse]Failed to process Response: $failureResponse")

        auditError(
          INTERNAL_SERVER_ERROR,
          ErrorMessage(s"Failed to process Response: $failureResponse", AUDIT_DEST_TECHNICAL_DIFFICULTIES)
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

  private def auditBalanceRequestNotMatched(errorPointer: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, request: DataRequest[_]): Unit = {
    val balanceRequestNotMatchedMessage = errorPointer match {
      case "RC1.TIN"                                 => AUDIT_ERROR_INCORRECT_EORI
      case "GRR(1).Guarantee reference number (GRN)" => AUDIT_ERROR_INCORRECT_GRN
      case "GRR(1).ACC(1).Access code"               => AUDIT_ERROR_INCORRECT_ACCESS_CODE
      case "GRR(1).OTG(1).TIN"                       => AUDIT_ERROR_EORI_GRN_DO_NOT_MATCH
      case _                                         => AUDIT_ERROR_DO_NOT_MATCH
    }

    auditError(
      SEE_OTHER,
      ErrorMessage(balanceRequestNotMatchedMessage, AUDIT_DEST_DETAILS_DO_NOT_MATCH)
    )
  }

  private def auditSuccess(successResponse: BalanceRequestSuccess)(implicit hc: HeaderCarrier, ec: ExecutionContext, request: DataRequest[_]): Unit =
    auditService.audit(
      SuccessfulBalanceAuditModel.build(
        request.userAnswers.get(EoriNumberPage).getOrElse("-"),
        request.userAnswers.get(GuaranteeReferenceNumberPage).getOrElse("-"),
        request.userAnswers.get(AccessCodePage).getOrElse("-"),
        request.internalId,
        LocalDateTime.now,
        OK,
        successResponse.currency.toString.trim + " " + successResponse.balance.toString.trim
      )
    )

  private def auditRateLimit()(implicit hc: HeaderCarrier, ec: ExecutionContext, request: DataRequest[_]): Unit =
    auditService.audit(
      UnsuccessfulBalanceAuditModel.build(
        AUDIT_TYPE_GUARANTEE_BALANCE_RATE_LIMIT,
        request.userAnswers.get(EoriNumberPage).getOrElse("-"),
        request.userAnswers.get(GuaranteeReferenceNumberPage).getOrElse("-"),
        request.userAnswers.get(AccessCodePage).getOrElse("-"),
        request.internalId,
        LocalDateTime.now,
        TOO_MANY_REQUESTS,
        ErrorMessage(AUDIT_ERROR_RATE_LIMIT_EXCEEDED, AUDIT_DEST_RATE_LIMITED)
      )
    )

  private def auditError(errorCode: Int, errorMessage: ErrorMessage)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext,
    request: DataRequest[_]
  ): Unit = {
    logger.warn(s"[GuaranteeBalanceResponseHandler][auditError]Failed to process errorMessage: $errorMessage, status Code: $errorCode")
    auditService.audit(
      UnsuccessfulBalanceAuditModel.build(
        AUDIT_TYPE_GUARANTEE_BALANCE_SUBMISSION,
        request.userAnswers.get(EoriNumberPage).getOrElse("-"),
        request.userAnswers.get(GuaranteeReferenceNumberPage).getOrElse("-"),
        request.userAnswers.get(AccessCodePage).getOrElse("-"),
        request.internalId,
        LocalDateTime.now,
        errorCode,
        errorMessage
      )
    )
  }
}
