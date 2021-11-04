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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.GuaranteeBalanceConnector
import models.UserAnswers
import models.backend.BalanceRequestSuccess
import models.requests.BalanceRequest
import models.values.{AccessCode, CurrencyCode, GuaranteeReference, TaxIdentifier}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.{AccessCodePage, BalancePage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

// format: off
class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with AppWithDefaultMockFixtures {

  private val grn: String    = "grn"
  private val access: String = "access"
  private val taxId: String  = "taxId"

  private val balance = BalanceRequestSuccess(8500, CurrencyCode("GBP"))

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(GuaranteeReferenceNumberPage, grn).success.value
    .set(AccessCodePage, access).success.value
    .set(EoriNumberPage, taxId).success.value

  "CheckYourAnswers Controller" - {

    "return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "checkYourAnswers.njk"

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

      val mockGuaranteeBalanceConnector = mock[GuaranteeBalanceConnector]

      val application = applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(bind[GuaranteeBalanceConnector].toInstance(mockGuaranteeBalanceConnector))
        .build()

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
      when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(true))

      when(mockGuaranteeBalanceConnector.submitBalanceRequest(any())(any()))
        .thenReturn(Future.successful(Right(balance)))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url

      val expectedLockId = (userAnswers.id + grn.trim.toLowerCase).hashCode.toString
      verify(mockMongoLockRepository).takeLock(eqTo(expectedLockId), eqTo(userAnswers.id), any())

      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(uaCaptor.capture)
      uaCaptor.getValue.get(BalancePage).get mustBe balance.formatForDisplay

      verify(mockGuaranteeBalanceConnector).submitBalanceRequest(eqTo(BalanceRequest(TaxIdentifier(taxId), GuaranteeReference(grn), AccessCode(access))))(any())
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
      val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
      when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(false))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.RateLimitController.onPageLoad().url

      val expectedLockId = (userAnswers.id + grn.trim.toLowerCase).hashCode.toString
      verify(mockMongoLockRepository).takeLock(eqTo(expectedLockId), eqTo(userAnswers.id), any())
    }

    "must redirect to session timeout if at least one of EORI, GRN and access code are undefined" in {

      forAll(arbitrary[(Option[String], Option[String], Option[String])].retryUntil {
        case (eoriNumber, grn, accessCode) => !(eoriNumber.isDefined && grn.isDefined && accessCode.isDefined)
      }) {
        case (eoriNumber, grn, accessCode) =>
          val userAnswers = emptyUserAnswers
            .setOption(EoriNumberPage, eoriNumber).success.value
            .setOption(GuaranteeReferenceNumberPage, grn).success.value
            .setOption(AccessCodePage, accessCode).success.value

          val mockGuaranteeBalanceConnector = mock[GuaranteeBalanceConnector]

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(bind[GuaranteeBalanceConnector].toInstance(mockGuaranteeBalanceConnector))
            .build()

          val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

          when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(true))

          when(mockGuaranteeBalanceConnector.submitBalanceRequest(any())(any()))
            .thenReturn(Future.successful(Right(balance)))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }
    }

  }
}
// format: on
