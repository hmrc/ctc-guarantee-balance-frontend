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

import base.{AppWithDefaultMockFixtures, SpecBase}
import matchers.JsonMatchers.containJson
import models.UserAnswers
import models.backend.BalanceRequestSuccess
import models.requests.BalanceRequest
import models.values.{AccessCode, CurrencyCode, GuaranteeReference, TaxIdentifier}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.{AccessCodePage, BalancePage, EoriNumberPage, GuaranteeReferenceNumberPage}
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

  private val balance = BalanceRequestSuccess(8500, CurrencyCode("GBP"))

  // format: off
  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(GuaranteeReferenceNumberPage, grn).success.value
    .set(AccessCodePage, access).success.value
    .set(EoriNumberPage, taxId).success.value
  // format: on

  private val mockViewModelProvider: CheckYourAnswersViewModelProvider = mock[CheckYourAnswersViewModelProvider]

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
        "section" -> emptySection
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

    "must redirect if successful" in {

      val userAnswers = baseAnswers
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request     = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

      when(mockGuaranteeBalanceService.submit(any(), any()))
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

  }
}
