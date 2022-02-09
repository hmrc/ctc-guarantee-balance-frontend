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
import models.backend.BalanceRequestSuccess
import models.values.CurrencyCode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.GuaranteeReferenceNumberPage
import play.api.test.FakeRequest
import play.api.test.Helpers._

import scala.concurrent.Future

class TryGuaranteeBalanceAgainControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val baseAnswers = emptyUserAnswers.set(GuaranteeReferenceNumberPage, "grn").success.value

  "TryGuaranteeBalanceAgainController" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      val request = FakeRequest(GET, routes.TryGuaranteeBalanceAgainController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustBe "tryGuaranteeBalanceAgain.njk"
    }

    "must redirect to session expired if GRN undefined in user answers" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, routes.TryGuaranteeBalanceAgainController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must pass the response from the submit onto the processor" in {

      val userAnswers = baseAnswers
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request     = FakeRequest(POST, routes.TryGuaranteeBalanceAgainController.onSubmit().url)

      when(mockGuaranteeBalanceService.submitBalanceRequest()(any(), any()))
        .thenReturn(Future.successful(Right(BalanceRequestSuccess(123.45, CurrencyCode("GBP")))))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url

      verify(mockGuaranteeBalanceService).submitBalanceRequest()(any(), any())
    }

  }
}
