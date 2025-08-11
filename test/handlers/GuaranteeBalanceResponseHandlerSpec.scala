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

import base.SpecBase
import cats.data.NonEmptyList
import models.UserAnswers
import models.backend.*
import models.backend.errors.FunctionalError
import models.requests.DataRequest
import models.values.{BalanceId, CurrencyCode, ErrorType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import pages.*
import play.api.mvc.{AnyContent, Request, Result, Results}
import play.api.test.Helpers.*
import repositories.SessionRepository
import services.AuditService
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse}
import viewModels.audit.AuditConstants.*
import viewModels.audit.{SuccessfulBalanceAuditModel, UnsuccessfulBalanceAuditModel}

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class GuaranteeBalanceResponseHandlerSpec extends SpecBase with BeforeAndAfterEach {

  private val expectedUuid    = UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4")
  private val balanceId       = BalanceId(expectedUuid)
  private val balanceResponse = BalanceRequestSuccess(BigDecimal(99.9), Some(CurrencyCode("GBP")))
  private val grn: String     = "grn"
  private val access: String  = "access"

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .setValue(GuaranteeReferenceNumberPage, grn)
    .setValue(AccessCodePage, access)

  private val baseAnswersWithBalanceId: UserAnswers = emptyUserAnswers
    .setValue(GuaranteeReferenceNumberPage, grn)
    .setValue(AccessCodePage, access)
    .setValue(BalanceIdPage, balanceId)

  private val noMatchResponse           = Right(BalanceRequestNotMatched("test"))
  private val eoriNoMatchResponse       = Right(BalanceRequestNotMatched("RC1.TIN"))
  private val grnNoMatchResponse        = Right(BalanceRequestNotMatched("GRR(1).Guarantee reference number (GRN)"))
  private val accessCodeNoMatchResponse = Right(BalanceRequestNotMatched("GRR(1).ACC(1).Access code"))
  private val eoriAndGrnMatchResponse   = Right(BalanceRequestNotMatched("GRR(1).OTG(1).TIN"))
  private val unsupportedTypeResponse   = Right(BalanceRequestUnsupportedGuaranteeType)
  private val successResponse           = Right(balanceResponse)
  private val pendingResponse           = Right(BalanceRequestPending(balanceId))
  private val tryAgainResponse          = Right(BalanceRequestPendingExpired(balanceId))
  private val tooManyRequestsResponse   = Right(BalanceRequestRateLimit)
  private val sessionExpiredResponse    = Right(BalanceRequestSessionExpired)
  private val httpErrorResponse         = Left(HttpResponse(404: Int, ""))

  private val functionalError      = FunctionalError(ErrorType(1), "", None)
  private val balanceErrorResponse = Right(BalanceRequestFunctionalError(NonEmptyList(functionalError, Nil)))

  implicit private val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  implicit private val request: DataRequest[?] = DataRequest(fakeRequest, "eoriNumber", baseAnswers)

  private lazy val mockSessionRepository: SessionRepository = mock[SessionRepository]
  private lazy val mockAuditService: AuditService           = mock[AuditService]
  private lazy val mockErrorHandler: ErrorHandler           = mock[ErrorHandler]

  private lazy val handler: GuaranteeBalanceResponseHandler =
    new GuaranteeBalanceResponseHandler(mockSessionRepository, mockAuditService, mockErrorHandler)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
    reset(mockAuditService)
    reset(mockErrorHandler)

    when(mockSessionRepository.set(any()))
      .thenReturn(Future.successful(true))
  }

  "GuaranteeBalanceResponseHandlerSpec" - {

    "must Redirect to the TryAgain Controller if the status is empty " in {
      val result: Future[Result] = handler.processResponse(tryAgainResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.TryAgainController.onPageLoad().url

      verify(mockSessionRepository, times(1)).set(any())
    }

    "must Redirect to the DetailsDontMatchController if the status is NoMatch " in {
      val result: Future[Result] = handler.processResponse(noMatchResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.DetailsDontMatchController.onPageLoad().url
      val auditCaptor: ArgumentCaptor[UnsuccessfulBalanceAuditModel] = ArgumentCaptor.forClass(classOf[UnsuccessfulBalanceAuditModel])

      verify(mockAuditService, times(1)).audit(auditCaptor.capture())(any(), any(), any())
      verify(mockSessionRepository, times(1)).set(any())

      auditCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_SUBMISSION
      auditCaptor.getValue.detail.toString.contains(AUDIT_ERROR_DO_NOT_MATCH) mustEqual true
      auditCaptor.getValue.detail.toString.contains(AUDIT_DEST_DETAILS_DO_NOT_MATCH) mustEqual true
    }

    "must Redirect to the DetailsDontMatchController if the status is Eori NoMatch " in {
      val result: Future[Result] = handler.processResponse(eoriNoMatchResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.DetailsDontMatchController.onPageLoad().url
      val auditCaptor: ArgumentCaptor[UnsuccessfulBalanceAuditModel] = ArgumentCaptor.forClass(classOf[UnsuccessfulBalanceAuditModel])

      verify(mockAuditService, times(1)).audit(auditCaptor.capture())(any(), any(), any())
      verify(mockSessionRepository, times(1)).set(any())

      auditCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_SUBMISSION
      auditCaptor.getValue.detail.toString.contains(AUDIT_ERROR_INCORRECT_EORI) mustEqual true
      auditCaptor.getValue.detail.toString.contains(AUDIT_DEST_DETAILS_DO_NOT_MATCH) mustEqual true
    }

    "must Redirect to the DetailsDontMatchController if the status is GRN NoMatch " in {
      val result: Future[Result] = handler.processResponse(grnNoMatchResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.DetailsDontMatchController.onPageLoad().url
      val auditCaptor: ArgumentCaptor[UnsuccessfulBalanceAuditModel] = ArgumentCaptor.forClass(classOf[UnsuccessfulBalanceAuditModel])

      verify(mockAuditService, times(1)).audit(auditCaptor.capture())(any(), any(), any())
      verify(mockSessionRepository, times(1)).set(any())

      auditCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_SUBMISSION
      auditCaptor.getValue.detail.toString.contains(AUDIT_ERROR_INCORRECT_GRN) mustEqual true
      auditCaptor.getValue.detail.toString.contains(AUDIT_DEST_DETAILS_DO_NOT_MATCH) mustEqual true
    }

    "must Redirect to the DetailsDontMatchController if the status is Access code NoMatch " in {
      val result: Future[Result] = handler.processResponse(accessCodeNoMatchResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.DetailsDontMatchController.onPageLoad().url
      val auditCaptor: ArgumentCaptor[UnsuccessfulBalanceAuditModel] = ArgumentCaptor.forClass(classOf[UnsuccessfulBalanceAuditModel])

      verify(mockAuditService, times(1)).audit(auditCaptor.capture())(any(), any(), any())
      verify(mockSessionRepository, times(1)).set(any())

      auditCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_SUBMISSION
      auditCaptor.getValue.detail.toString.contains(AUDIT_ERROR_INCORRECT_ACCESS_CODE) mustEqual true
      auditCaptor.getValue.detail.toString.contains(AUDIT_DEST_DETAILS_DO_NOT_MATCH) mustEqual true
    }

    "must Redirect to the DetailsDontMatchController if the status is GRN and Eori NoMatch " in {
      val result: Future[Result] = handler.processResponse(eoriAndGrnMatchResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.DetailsDontMatchController.onPageLoad().url
      val auditCaptor: ArgumentCaptor[UnsuccessfulBalanceAuditModel] = ArgumentCaptor.forClass(classOf[UnsuccessfulBalanceAuditModel])

      verify(mockAuditService, times(1)).audit(auditCaptor.capture())(any(), any(), any())
      verify(mockSessionRepository, times(1)).set(any())

      auditCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_SUBMISSION
      auditCaptor.getValue.detail.toString.contains(AUDIT_ERROR_EORI_GRN_DO_NOT_MATCH) mustEqual true
      auditCaptor.getValue.detail.toString.contains(AUDIT_DEST_DETAILS_DO_NOT_MATCH) mustEqual true
    }

    "must Redirect to the UnsupportedGuaranteeTypeController if the status is Unsupported Type " in {
      val result: Future[Result] = handler.processResponse(unsupportedTypeResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.UnsupportedGuaranteeTypeController.onPageLoad().url

      verify(mockSessionRepository, times(1)).set(any())
    }

    "must return back to the wait page if the status is still pending " in {
      val result: Future[Result] = handler.processResponse(pendingResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.TryAgainController.onPageLoad().url

      val userAnswersCapture: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository, times(2)).set(userAnswersCapture.capture())
      userAnswersCapture.getAllValues.get(1).get(BalanceIdPage).get mustEqual balanceId

      verify(mockAuditService, times(0)).audit(any())(any(), any(), any())
    }

    "must Redirect to the Balance Confirmation Controller if the status is DataReturned " in {
      val mockRequest                      = mock[Request[AnyContent]]
      implicit val request: DataRequest[?] = DataRequest(mockRequest, "eoriNumber", baseAnswersWithBalanceId)

      val result: Future[Result] = handler.processResponse(successResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.BalanceConfirmationController.onPageLoad().url

      val auditCaptor: ArgumentCaptor[SuccessfulBalanceAuditModel] = ArgumentCaptor.forClass(classOf[SuccessfulBalanceAuditModel])

      verify(mockAuditService, times(1)).audit(auditCaptor.capture())(any(), any(), any())

      auditCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_SUBMISSION
      auditCaptor.getValue.status mustEqual OK

      val userAnswersCapture: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository, times(2)).set(userAnswersCapture.capture())
      val finalUserAnswers = userAnswersCapture.getAllValues.get(1)
      finalUserAnswers.get(BalanceIdPage).isDefined mustEqual false
      finalUserAnswers.get(BalancePage).get mustEqual balanceResponse.formatForDisplay
    }

    "must Redirect to the session expires page if we have a session expired response" in {
      val result: Future[Result] = handler.processResponse(sessionExpiredResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      verify(mockAuditService, times(0)).audit(any())(any(), any(), any())
      verify(mockSessionRepository, times(1)).set(any())

    }

    "must Redirect show the technical difficulties page if it has a processErrorResponse" in {
      val url = "/technical-difficulties"

      when(mockErrorHandler.onClientError(any(), any(), any()))
        .thenReturn(Future.successful(Results.SeeOther(url)))

      val result: Future[Result] = handler.processResponse(balanceErrorResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual url

      verify(mockSessionRepository, times(1)).set(any())
    }

    "must Redirect to the rate limit page if we have a RateLimit Response" in {
      val result: Future[Result] = handler.processResponse(tooManyRequestsResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.TryAgainController.onPageLoad().url

      val auditCaptor: ArgumentCaptor[UnsuccessfulBalanceAuditModel] = ArgumentCaptor.forClass(classOf[UnsuccessfulBalanceAuditModel])

      verify(mockAuditService, times(1)).audit(auditCaptor.capture())(any(), any(), any())
      verify(mockSessionRepository, times(1)).set(any())

      auditCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_RATE_LIMIT
      auditCaptor.getValue.detail.toString.contains(AUDIT_ERROR_RATE_LIMIT_EXCEEDED) mustEqual true
      auditCaptor.getValue.detail.toString.contains(AUDIT_DEST_RATE_LIMITED) mustEqual true
    }

    "must Redirect show the technical difficulties page if it has a httpResponseError" in {
      val url = "/technical-difficulties"

      when(mockErrorHandler.onClientError(any(), any(), any()))
        .thenReturn(Future.successful(Results.SeeOther(url)))

      val result: Future[Result] = handler.processResponse(httpErrorResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual url

      verify(mockSessionRepository, times(1)).set(any())
    }
  }

}
