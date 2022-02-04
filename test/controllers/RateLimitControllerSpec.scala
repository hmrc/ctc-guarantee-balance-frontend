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
import models.UserAnswers
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import pages.{AccessCodePage, EoriNumberPage, GuaranteeReferenceNumberPage}

import scala.concurrent.Future

class RateLimitControllerSpec extends SpecBase with MockitoSugar with AppWithDefaultMockFixtures {

  private val grn: String    = "grn"
  private val access: String = "access"
  private val taxId: String  = "taxId"

  // format: off
  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(GuaranteeReferenceNumberPage, grn).success.value
    .set(AccessCodePage, access).success.value
    .set(EoriNumberPage, taxId).success.value
  // format: on

  "RateLimit Controller" - {

    "return OK and the correct view for a GET" in {

      val application                            = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request                                = FakeRequest(GET, routes.RateLimitController.onPageLoad().url)
      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

      val result = route(application, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "rateLimit.njk"

      application.stop()
    }

    "must redirect if POST successful" in {

      val userAnswers = baseAnswers
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request     = FakeRequest(POST, routes.RateLimitController.onSubmit().url)

      when(mockGuaranteeBalanceService.submitBalanceRequest(any(), any())(any(), any()))
        .thenReturn(Future.successful(Redirect(routes.BalanceConfirmationController.onPageLoad().url)))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url

      verify(mockGuaranteeBalanceService).submitBalanceRequest(eqTo(userAnswers), any())(any(), any())
    }

  }
}
