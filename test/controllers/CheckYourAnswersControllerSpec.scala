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

import java.util.UUID

import base.{AppWithDefaultMockFixtures, SpecBase}
import matchers.JsonMatchers.containJson
import models.UserAnswers
import models.backend.BalanceRequestSuccess
import models.requests.BalanceRequest
import models.values.{AccessCode, BalanceId, CurrencyCode, GuaranteeReference, TaxIdentifier}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.{AccessCodePage, BalanceIdPage, BalancePage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.{AuditService, JsonAuditModel}
import viewModels.audit.AuditConstants.{AUDIT_DEST_RATE_LIMITED, AUDIT_ERROR_RATE_LIMIT_EXCEEDED, AUDIT_TYPE_GUARANTEE_BALANCE_RATE_LIMIT}
import viewModels.{CheckYourAnswersViewModel, CheckYourAnswersViewModelProvider, Section}

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with AppWithDefaultMockFixtures {

  private val grn: String    = "grn"
  private val access: String = "access"
  private val taxId: String  = "taxId"
  private val expectedUuid   = UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4")
  private val balanceId      = BalanceId(expectedUuid)

  private val balance = BalanceRequestSuccess(8500, CurrencyCode("GBP"))

  // format: off
  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(GuaranteeReferenceNumberPage, grn).success.value
    .set(AccessCodePage, access).success.value
    .set(EoriNumberPage, taxId).success.value
  // format: on

  private val mockViewModelProvider: CheckYourAnswersViewModelProvider = mock[CheckYourAnswersViewModelProvider]
  private lazy val auditService: AuditService                          = app.injector.instanceOf[AuditService]

  override protected def applicationBuilder(userAnswers: Option[UserAnswers]): GuiceApplicationBuilder =
    super
      .applicationBuilder(userAnswers)
      .overrides(bind[CheckYourAnswersViewModelProvider].toInstance(mockViewModelProvider))

  private val emptySection: Section = Section(Nil)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
    when(mockViewModelProvider(any())).thenReturn(CheckYourAnswersViewModel(emptySection))
  }

  "CheckYourAnswers Controller" - {

    "return OK and the correct view for a GET" in {
      val userAnswers = baseAnswers
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request     = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "section"   -> emptySection,
        "submitUrl" -> routes.CheckYourAnswersController.onSubmit().url
      )

      templateCaptor.getValue mustEqual "checkYourAnswers.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      verify(mockViewModelProvider)(userAnswers)

      application.stop()
    }

    "return OK and the correct view for a GET when we have a BalanceId set" in {
      val userAnswers = baseAnswers.set(BalanceIdPage, balanceId).success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request     = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "section"   -> emptySection,
        "submitUrl" -> routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId).url
      )

      templateCaptor.getValue mustEqual "checkYourAnswers.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      verify(mockViewModelProvider)(userAnswers)

      application.stop()
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Balance Confirmation for a POST if no lock in mongo repository for that user and GRN" in {

      val userAnswers = baseAnswers
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request     = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

      when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(true))

      when(mockGuaranteeBalanceService.submitBalanceRequest(any())(any()))
        .thenReturn(Future.successful(Right(balance)))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url

      val expectedLockId = (userAnswers.id + grn.trim.toLowerCase).hashCode.toString
      verify(mockMongoLockRepository).takeLock(eqTo(expectedLockId), eqTo(userAnswers.id), any())

      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(uaCaptor.capture)
      uaCaptor.getValue.get(BalancePage).get mustBe balance.formatForDisplay

      verify(mockGuaranteeBalanceService).submitBalanceRequest(eqTo(BalanceRequest(TaxIdentifier(taxId), GuaranteeReference(grn), AccessCode(access))))(any())
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to rate limit if lock in mongo repository for that user and GRN" in {

      val userAnswers = baseAnswers
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request     = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

      when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(false))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.RateLimitController.onPageLoad().url

      val expectedLockId = (userAnswers.id + grn.trim.toLowerCase).hashCode.toString
      verify(mockMongoLockRepository).takeLock(eqTo(expectedLockId), eqTo(userAnswers.id), any())

      val jsonCaptor: ArgumentCaptor[JsonAuditModel] = ArgumentCaptor.forClass(classOf[JsonAuditModel])

      verify(auditService, times(1)).audit(jsonCaptor.capture())(any(), any(), any())

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

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }
    }
  }
}
