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
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import pages.GuaranteeReferenceNumberPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

class TryGuaranteeBalanceAgainControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  "TryGuaranteeBalanceAgainController" - {

    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers.set(GuaranteeReferenceNumberPage, "grn").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val request = FakeRequest(GET, routes.TryGuaranteeBalanceAgainController.onPageLoad().url)

      val result = route(application, request).value

      status(result) mustEqual OK

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustBe "tryGuaranteeBalanceAgain.njk"
    }

    "must release lock" in {

      val userAnswers = emptyUserAnswers.set(GuaranteeReferenceNumberPage, "grn").success.value
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
      val request     = FakeRequest(GET, routes.TryGuaranteeBalanceAgainController.onPageLoad().url)

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val result = route(application, request).value

      status(result) mustEqual OK

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustBe "tryGuaranteeBalanceAgain.njk"

      val expectedLockId = (userAnswers.id + "grn".trim.toLowerCase).hashCode.toString
      verify(mockMongoLockRepository).releaseLock(eqTo(expectedLockId), eqTo(userAnswers.id))

    }

    "onSubmit" - {
      "must Redirect to the Check Your Answers Controller if the status is DataReturned " in {
        val request = FakeRequest(POST, routes.TryGuaranteeBalanceAgainController.onSubmit().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onSubmit().url
      }
    }
  }
}
