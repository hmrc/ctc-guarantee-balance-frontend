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

import java.util.UUID

import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptyList
import matchers.JsonMatchers
import models.UserAnswers
import models.backend._
import models.backend.errors.FunctionalError
import models.requests.DataRequest
import models.values.{BalanceId, CurrencyCode, ErrorType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify}
import pages.{BalanceIdPage, BalancePage, GuaranteeReferenceNumberPage}
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.Helpers._
import services.AuditService
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse}
import viewModels.audit.AuditConstants._
import viewModels.audit.{SuccessfulBalanceAuditModel, UnsuccessfulBalanceAuditModel}

import scala.concurrent.Future

class GuaranteeBalanceResponseHandlerSpec extends SpecBase with JsonMatchers with AppWithDefaultMockFixtures {

  val expectedUuid         = UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4")
  val balanceId            = BalanceId(expectedUuid)
  val balanceResponse      = BalanceRequestSuccess(BigDecimal(99.9), CurrencyCode("GBP"))
  private val grn: String  = "grn"
  val populatedUserAnswers = emptyUserAnswers.set(GuaranteeReferenceNumberPage, grn).success.value

  val noMatchResponse           = Right(BalanceRequestNotMatched("test"))
  val eoriNoMatchResponse       = Right(BalanceRequestNotMatched("RC1.TIN"))
  val grnNoMatchResponse        = Right(BalanceRequestNotMatched("GRR(1).Guarantee reference number (GRN)"))
  val accessCodeNoMatchResponse = Right(BalanceRequestNotMatched("GRR(1).ACC(1).Access code"))
  val eoriAndGrnMatchResponse   = Right(BalanceRequestNotMatched("GRR(1).OTG(1).TIN"))
  val unsupportedTypeResponse   = Right(BalanceRequestUnsupportedGuaranteeType)
  val successResponse           = Right(balanceResponse)
  val pendingResponse           = Right(BalanceRequestPending(balanceId))
  val tryAgainResponse          = Right(BalanceRequestPendingExpired(balanceId))
  val tooManyRequestsResponse   = Right(BalanceRequestRateLimit)
  val sessionExpiredResponse    = Right(BalanceRequestSessionExpired)
  val httpErrorResponse         = Left(HttpResponse(404, ""))

  val functionalError      = FunctionalError(ErrorType(1), "", None)
  val balanceErrorResponse = Right(BalanceRequestFunctionalError(NonEmptyList(functionalError, Nil)))

  implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  val mockRequest      = mock[Request[AnyContent]]
  implicit val request = DataRequest(mockRequest, "eoriNumber", populatedUserAnswers)

  private lazy val handler: GuaranteeBalanceResponseHandler = app.injector.instanceOf[GuaranteeBalanceResponseHandler]
  private lazy val auditService: AuditService               = app.injector.instanceOf[AuditService]

  "GuaranteeBalanceResponseHandlerSpec" - {

    "must Redirect to the TryAgain Controller if the status is empty " in {
      val result: Future[Result] = handler.processResponse(tryAgainResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.TryAgainController.onPageLoad().url

      verify(mockSessionRepository, times(0)).set(any())
    }

    "must Redirect to the DetailsDontMatchController if the status is NoMatch " in {
      val result: Future[Result] = handler.processResponse(noMatchResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.DetailsDontMatchController.onPageLoad().url
      val jsonCaptor: ArgumentCaptor[UnsuccessfulBalanceAuditModel] = ArgumentCaptor.forClass(classOf[UnsuccessfulBalanceAuditModel])

      verify(auditService, times(1)).audit(jsonCaptor.capture())(any(), any(), any())
      verify(mockSessionRepository, times(0)).set(any())

      jsonCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_SUBMISSION
      jsonCaptor.getValue.detail.toString.contains(AUDIT_ERROR_DO_NOT_MATCH) mustEqual true
      jsonCaptor.getValue.detail.toString.contains(AUDIT_DEST_DETAILS_DO_NOT_MATCH) mustEqual true
    }

    "must Redirect to the DetailsDontMatchController if the status is Eori NoMatch " in {
      val result: Future[Result] = handler.processResponse(eoriNoMatchResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.DetailsDontMatchController.onPageLoad().url
      val jsonCaptor: ArgumentCaptor[UnsuccessfulBalanceAuditModel] = ArgumentCaptor.forClass(classOf[UnsuccessfulBalanceAuditModel])

      verify(auditService, times(1)).audit(jsonCaptor.capture())(any(), any(), any())
      verify(mockSessionRepository, times(0)).set(any())

      jsonCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_SUBMISSION
      jsonCaptor.getValue.detail.toString.contains(AUDIT_ERROR_INCORRECT_EORI) mustEqual true
      jsonCaptor.getValue.detail.toString.contains(AUDIT_DEST_DETAILS_DO_NOT_MATCH) mustEqual true
    }

    "must Redirect to the DetailsDontMatchController if the status is GRN NoMatch " in {
      val result: Future[Result] = handler.processResponse(grnNoMatchResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.DetailsDontMatchController.onPageLoad().url
      val jsonCaptor: ArgumentCaptor[UnsuccessfulBalanceAuditModel] = ArgumentCaptor.forClass(classOf[UnsuccessfulBalanceAuditModel])

      verify(auditService, times(1)).audit(jsonCaptor.capture())(any(), any(), any())
      verify(mockSessionRepository, times(0)).set(any())

      jsonCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_SUBMISSION
      jsonCaptor.getValue.detail.toString.contains(AUDIT_ERROR_INCORRECT_GRN) mustEqual true
      jsonCaptor.getValue.detail.toString.contains(AUDIT_DEST_DETAILS_DO_NOT_MATCH) mustEqual true
    }

    "must Redirect to the DetailsDontMatchController if the status is Access code NoMatch " in {
      val result: Future[Result] = handler.processResponse(accessCodeNoMatchResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.DetailsDontMatchController.onPageLoad().url
      val jsonCaptor: ArgumentCaptor[UnsuccessfulBalanceAuditModel] = ArgumentCaptor.forClass(classOf[UnsuccessfulBalanceAuditModel])

      verify(auditService, times(1)).audit(jsonCaptor.capture())(any(), any(), any())
      verify(mockSessionRepository, times(0)).set(any())

      jsonCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_SUBMISSION
      jsonCaptor.getValue.detail.toString.contains(AUDIT_ERROR_INCORRECT_ACCESS_CODE) mustEqual true
      jsonCaptor.getValue.detail.toString.contains(AUDIT_DEST_DETAILS_DO_NOT_MATCH) mustEqual true
    }

    "must Redirect to the DetailsDontMatchController if the status is GRN and Eori NoMatch " in {
      val result: Future[Result] = handler.processResponse(eoriAndGrnMatchResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.DetailsDontMatchController.onPageLoad().url
      val jsonCaptor: ArgumentCaptor[UnsuccessfulBalanceAuditModel] = ArgumentCaptor.forClass(classOf[UnsuccessfulBalanceAuditModel])

      verify(auditService, times(1)).audit(jsonCaptor.capture())(any(), any(), any())
      verify(mockSessionRepository, times(0)).set(any())

      jsonCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_SUBMISSION
      jsonCaptor.getValue.detail.toString.contains(AUDIT_ERROR_EORI_GRN_DO_NOT_MATCH) mustEqual true
      jsonCaptor.getValue.detail.toString.contains(AUDIT_DEST_DETAILS_DO_NOT_MATCH) mustEqual true
    }

    "must Redirect to the UnsupportedGuaranteeTypeController if the status is Unsupported Type " in {
      val result: Future[Result] = handler.processResponse(unsupportedTypeResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.UnsupportedGuaranteeTypeController.onPageLoad().url

      verify(mockSessionRepository, times(0)).set(any())
    }

    "must return back to the wait page if the status is still pending " in {
      val result: Future[Result] = handler.processResponse(pendingResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.TryAgainController.onPageLoad().url

      val userAnswersCapture: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository, times(1)).set(userAnswersCapture.capture())
      userAnswersCapture.getValue.get(BalanceIdPage) mustEqual Some(balanceId)
      verify(auditService, times(0)).audit(any())(any(), any(), any())
    }

    "must Redirect to the Balance Confirmation Controller if the status is DataReturned " in {
      val result: Future[Result] = handler.processResponse(successResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.BalanceConfirmationController.onPageLoad().url

      val jsonCaptor: ArgumentCaptor[SuccessfulBalanceAuditModel] = ArgumentCaptor.forClass(classOf[SuccessfulBalanceAuditModel])

      verify(auditService, times(1)).audit(jsonCaptor.capture())(any(), any(), any())

      jsonCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_SUBMISSION
      jsonCaptor.getValue.status mustEqual OK

      val userAnswersCapture: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository, times(1)).set(userAnswersCapture.capture())
      userAnswersCapture.getValue.get(BalancePage) mustEqual Some(balanceResponse.formatForDisplay)
    }

    "must Redirect to the session expires page if we have a session expired response" in {
      val result: Future[Result] = handler.processResponse(sessionExpiredResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url

      verify(auditService, times(0)).audit(any())(any(), any(), any())
      verify(mockSessionRepository, times(0)).set(any())

    }

    "must Redirect show the technical difficulties page if it has a processErrorResponse " in {
      val result: Future[Result] = handler.processResponse(balanceErrorResponse)

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      status(result) mustEqual INTERNAL_SERVER_ERROR
      templateCaptor.getValue mustBe "technicalDifficulties.njk"
    }

    "must Redirect to the rate limit page if we have a RateLimit Response" in {
      val result: Future[Result] = handler.processResponse(tooManyRequestsResponse)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.TryAgainController.onPageLoad().url

      val jsonCaptor: ArgumentCaptor[UnsuccessfulBalanceAuditModel] = ArgumentCaptor.forClass(classOf[UnsuccessfulBalanceAuditModel])

      verify(auditService, times(1)).audit(jsonCaptor.capture())(any(), any(), any())
      verify(mockSessionRepository, times(0)).set(any())

      jsonCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_RATE_LIMIT
      jsonCaptor.getValue.detail.toString.contains(AUDIT_ERROR_RATE_LIMIT_EXCEEDED) mustEqual true
      jsonCaptor.getValue.detail.toString.contains(AUDIT_DEST_RATE_LIMITED) mustEqual true
    }

    "must Redirect show the technical difficulties page if it has a httpResponseError " in {
      val result: Future[Result] = handler.processResponse(httpErrorResponse)

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      status(result) mustEqual INTERNAL_SERVER_ERROR
      templateCaptor.getValue mustBe "technicalDifficulties.njk"
    }
  }

}
