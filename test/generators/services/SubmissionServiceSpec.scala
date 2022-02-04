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

package services

import base.{AppWithDefaultMockFixtures, SpecBase}
import config.FrontendAppConfig
import controllers.routes
import handlers.GuaranteeBalanceResponseHandler
import models.UserAnswers
import models.backend.BalanceRequestSuccess
import models.requests.{BalanceRequest, DataRequest}
import models.values.{AccessCode, CurrencyCode, GuaranteeReference, TaxIdentifier}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{AccessCodePage, BalancePage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.scalacheck.Arbitrary.arbitrary
import viewModels.audit.AuditConstants.{AUDIT_DEST_RATE_LIMITED, AUDIT_ERROR_RATE_LIMIT_EXCEEDED, AUDIT_TYPE_GUARANTEE_BALANCE_RATE_LIMIT}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class SubmissionServiceSpec extends SpecBase with MockitoSugar with AppWithDefaultMockFixtures {

  private val grn: String                                                      = "grn"
  private val access: String                                                   = "access"
  private val taxId: String                                                    = "taxId"
  private val guaranteeBalanceResponseHandler: GuaranteeBalanceResponseHandler = injector.instanceOf[GuaranteeBalanceResponseHandler]
  private val config: FrontendAppConfig                                        = injector.instanceOf[FrontendAppConfig]
  private val balance                                                          = BalanceRequestSuccess(8500, CurrencyCode("GBP"))
  implicit private val hc: HeaderCarrier                                       = HeaderCarrier(Some(Authorization("BearerToken")))
  private val request                                                          = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
  implicit private val dataRequest                                             = DataRequest(request, "id", emptyUserAnswers)

  // format: off
  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(GuaranteeReferenceNumberPage, grn).success.value
    .set(AccessCodePage, access).success.value
    .set(EoriNumberPage, taxId).success.value
  // format: on

  "Submission service" - {

    "must redirect to Session Expired for a POST if no existing data is found" in {

      val request              = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
      implicit val dataRequest = DataRequest(request, "internalId", emptyUserAnswers)

      implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

      val service = new SubmissionService(mockGuaranteeBalanceService, guaranteeBalanceResponseHandler, mockAuditService, mockMongoLockRepository, config)
      val result  = service.submit(emptyUserAnswers, "internalId")

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Balance Confirmation for a POST if no lock in mongo repository for that user and GRN" in {

      val userAnswers = baseAnswers

      when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(true))
      when(mockGuaranteeBalanceService.submitBalanceRequest(any())(any()))
        .thenReturn(Future.successful(Right(balance)))

      val service = new SubmissionService(mockGuaranteeBalanceService, guaranteeBalanceResponseHandler, mockAuditService, mockMongoLockRepository, config)
      val result  = service.submit(userAnswers, "id")

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url

      val expectedLockId = (userAnswers.id + grn.trim.toLowerCase).hashCode.toString
      verify(mockMongoLockRepository).takeLock(eqTo(expectedLockId), eqTo(userAnswers.id), any())
      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(uaCaptor.capture)
      uaCaptor.getValue.get(BalancePage).get mustBe balance.formatForDisplay

      verify(mockGuaranteeBalanceService).submitBalanceRequest(eqTo(BalanceRequest(TaxIdentifier(taxId), GuaranteeReference(grn), AccessCode(access))))(any())
    }
    "must redirect to rate limit if lock in mongo repository for that user and GRN" in {

      val userAnswers = baseAnswers

      when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(false))
      val service = new SubmissionService(mockGuaranteeBalanceService, guaranteeBalanceResponseHandler, mockAuditService, mockMongoLockRepository, config)
      val result  = service.submit(userAnswers, "id")

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.RateLimitController.onPageLoad().url

      val expectedLockId = (userAnswers.id + grn.trim.toLowerCase).hashCode.toString
      verify(mockMongoLockRepository).takeLock(eqTo(expectedLockId), eqTo(userAnswers.id), any())

      val jsonCaptor: ArgumentCaptor[JsonAuditModel] = ArgumentCaptor.forClass(classOf[JsonAuditModel])

      verify(mockAuditService, times(1)).audit(jsonCaptor.capture())(any(), any(), any())

      jsonCaptor.getValue.auditType mustEqual AUDIT_TYPE_GUARANTEE_BALANCE_RATE_LIMIT
      jsonCaptor.getValue.detail.toString.contains(AUDIT_ERROR_RATE_LIMIT_EXCEEDED) mustEqual true
      jsonCaptor.getValue.detail.toString.contains(AUDIT_DEST_RATE_LIMITED) mustEqual true
    }

    "must redirect to session timeout if at least one of EORI, GRN and access code are undefined" in {

      forAll(arbitrary[(Option[String], Option[String], Option[String])].retryUntil {
        case (eoriNumber, grn, accessCode) => !(eoriNumber.isDefined && grn.isDefined && accessCode.isDefined)
      }) {
        case (eoriNumber, grn, accessCode) =>
          // format: off
          val userAnswers = emptyUserAnswers
            .setOption(EoriNumberPage, eoriNumber).success.value
            .setOption(GuaranteeReferenceNumberPage, grn).success.value
            .setOption(AccessCodePage, accessCode).success.value
          // format: on

          val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

          val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

          when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(true))

          when(mockGuaranteeBalanceService.submitBalanceRequest(any())(any()))
            .thenReturn(Future.successful(Right(balance)))

          val service = new SubmissionService(mockGuaranteeBalanceService, guaranteeBalanceResponseHandler, mockAuditService, mockMongoLockRepository, config)
          val result  = service.submit(userAnswers, "id")
          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }
    }

  }
}
