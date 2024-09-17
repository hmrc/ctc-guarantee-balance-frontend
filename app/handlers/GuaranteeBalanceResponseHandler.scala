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

package handlers

import models.UserAnswers
import models.backend._
import models.requests.DataRequest
import pages._
import play.api.Logging
import play.api.http.Status._
import play.api.mvc.Results.Redirect
import play.api.mvc._
import repositories.SessionRepository
import services.AuditService
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import viewModels.audit.AuditConstants._
import viewModels.audit.{ErrorMessage, SuccessfulBalanceAuditModel, UnsuccessfulBalanceAuditModel}

import java.time.LocalDateTime
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

sealed trait GuaranteeBalanceResponseHandler extends Logging {

  val sessionRepository: SessionRepository
  val auditService: AuditService
  val errorHandler: ErrorHandler

  implicit val ec: ExecutionContext

  def detailsDoNotMatch: Call

  def processResponse(response: Either[HttpResponse, BalanceRequestResponse])(implicit
    hc: HeaderCarrier,
    request: DataRequest[?]
  ): Future[Result] =
    for {
      updateAnswers <- removeBalanceIdFromUserAnswers()
      handlerResponse <- response match {
        case Right(balanceRequestResponse) => processBalanceRequestResponse(balanceRequestResponse, updateAnswers)
        case Left(failureResponse)         => processHttpResponse(failureResponse)
      }
    } yield handlerResponse

  private def processBalanceRequestResponse(response: BalanceRequestResponse, userAnswers: UserAnswers)(implicit
    hc: HeaderCarrier,
    request: DataRequest[?]
  ): Future[Result] =
    response match {
      case pendingResponse: BalanceRequestPending =>
        processPendingResponse(pendingResponse, userAnswers)

      case successResponse: BalanceRequestSuccess =>
        auditSuccess(successResponse)
        processSuccessResponse(successResponse, userAnswers)

      case BalanceRequestSessionExpired =>
        logger.info("[GuaranteeBalanceResponseHandler][processBalanceRequestResponse] BalanceRequestSessionExpired")
        Future.successful(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))

      case BalanceRequestNotMatched(errorPointer) =>
        auditBalanceRequestNotMatched(errorPointer)
        Future.successful(Redirect(detailsDoNotMatch))

      case BalanceRequestRateLimit =>
        auditRateLimit()
        Future.successful(Redirect(controllers.routes.TryAgainController.onPageLoad()))

      case BalanceRequestPendingExpired(_) =>
        auditError(
          SEE_OTHER,
          ErrorMessage(AUDIT_ERROR_REQUEST_EXPIRED, AUDIT_DEST_TRY_AGAIN)
        )
        Future.successful(Redirect(controllers.routes.TryAgainController.onPageLoad()))

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
        errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
    }

  private def processHttpResponse(failureResponse: HttpResponse)(implicit hc: HeaderCarrier, request: DataRequest[?]): Future[Result] = {
    logger.warn(s"[GuaranteeBalanceResponseHandler][processHttpResponse] Failed to process Response: $failureResponse")

    auditError(
      INTERNAL_SERVER_ERROR,
      ErrorMessage(s"Failed to process Response: $failureResponse", AUDIT_DEST_TECHNICAL_DIFFICULTIES)
    )
    errorHandler.onClientError(request, INTERNAL_SERVER_ERROR)
  }

  private def processPendingResponse(balanceResponse: BalanceRequestPending, userAnswers: UserAnswers): Future[Result] = {
    logger.info("[GuaranteeBalanceResponseHandler][processBalanceRequestResponse] BalanceRequestPending")
    for {
      updatedAnswers <- Future.fromTry(userAnswers.set(BalanceIdPage, balanceResponse.balanceId))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(controllers.routes.TryAgainController.onPageLoad())
  }

  private def processSuccessResponse(balanceResponse: BalanceRequestSuccess, userAnswers: UserAnswers): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(userAnswers.set(BalancePage, balanceResponse.formatForDisplay))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(controllers.routes.BalanceConfirmationController.onPageLoad())

  private def removeBalanceIdFromUserAnswers()(implicit request: DataRequest[?]): Future[UserAnswers] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.remove(BalanceIdPage))
      _              <- sessionRepository.set(updatedAnswers)
    } yield updatedAnswers

  private def auditBalanceRequestNotMatched(errorPointer: String)(implicit hc: HeaderCarrier, ec: ExecutionContext, request: DataRequest[?]): Unit = {
    val balanceRequestNotMatchedMessage = errorPointer match {
      case "RC1.TIN"                                 => AUDIT_ERROR_INCORRECT_EORI
      case "GRR(1).Guarantee reference number (GRN)" => AUDIT_ERROR_INCORRECT_GRN
      case "GRR(1).ACC(1).Access code"               => AUDIT_ERROR_INCORRECT_ACCESS_CODE
      case "GRR(1).OTG(1).TIN"                       => AUDIT_ERROR_EORI_GRN_DO_NOT_MATCH
      case _                                         => AUDIT_ERROR_DO_NOT_MATCH
    }
    logger.info(s"[GuaranteeBalanceResponseHandler][auditBalanceRequestNotMatched] Failed to match $errorPointer")
    auditError(
      SEE_OTHER,
      ErrorMessage(balanceRequestNotMatchedMessage, AUDIT_DEST_DETAILS_DO_NOT_MATCH)
    )
  }

  private def auditSuccess(successResponse: BalanceRequestSuccess)(implicit hc: HeaderCarrier, ec: ExecutionContext, request: DataRequest[?]): Unit = {
    logger.info(s"[GuaranteeBalanceResponseHandler][auditSuccess]")

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
  }

  private def auditRateLimit()(implicit hc: HeaderCarrier, ec: ExecutionContext, request: DataRequest[?]): Unit = {
    logger.info(s"[GuaranteeBalanceResponseHandler][auditRateLimit] Request limit exceeded")
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
  }

  private def auditError(errorCode: Int, errorMessage: ErrorMessage)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext,
    request: DataRequest[?]
  ): Unit = {
    logger.warn(s"[GuaranteeBalanceResponseHandler][auditError] Failed to process errorMessage: $errorMessage, status Code: $errorCode")
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

class GuaranteeBalanceResponseHandlerV1 @Inject() (
  override val sessionRepository: SessionRepository,
  override val auditService: AuditService,
  override val errorHandler: ErrorHandler
)(implicit override val ec: ExecutionContext)
    extends GuaranteeBalanceResponseHandler {

  override def detailsDoNotMatch: Call = controllers.routes.DetailsDontMatchControllerV1.onPageLoad()
}

class GuaranteeBalanceResponseHandlerV2 @Inject() (
  override val sessionRepository: SessionRepository,
  override val auditService: AuditService,
  override val errorHandler: ErrorHandler
)(implicit override val ec: ExecutionContext)
    extends GuaranteeBalanceResponseHandler {

  override def detailsDoNotMatch: Call = controllers.routes.DetailsDontMatchControllerV2.onPageLoad()
}
