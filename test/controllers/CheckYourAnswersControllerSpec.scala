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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.twirl.api.Html

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with AppWithDefaultMockFixtures {

  "CheckYourAnswers Controller" - {

    "return OK and the correct view for a GET" in {

      when(mockRenderer.render(any(), any())(any()))
        .thenReturn(Future.successful(Html("")))

      val application                            = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
      val request                                = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
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

    "must redirect to Balance Confirmation for a POST" in {

      val application = applicationBuilder(userAnswers = Some(nonEmptyUserAnswers)).build()
      val request     = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
      when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(true))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      val request = FakeRequest(GET, routes.CheckYourAnswersController.onSubmit().url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to session expired if GRN is not found in user answer " in {

      val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

    }

    "must redirect to rate limit if lock in mongo repository is taken for that user and GRN " in {
      val application = applicationBuilder(userAnswers = Some(nonEmptyUserAnswers)).build()
      val request     = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
      when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(false))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.RateLimitController.onPageLoad().url

    }

  }
}
