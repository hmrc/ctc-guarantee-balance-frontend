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

package controllers.v2

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import models.UserAnswers
import models.backend.BalanceRequestSuccess
import models.values.CurrencyCode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.{AccessCodePage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.CheckYourAnswersViewModelV2.CheckYourAnswersViewModelProviderV2
import viewModels.{CheckYourAnswersViewModelV2, Section}
import views.html.v2.CheckYourAnswersViewV2

import scala.concurrent.Future

class CheckYourAnswersControllerV2Spec extends SpecBase with AppWithDefaultMockFixtures {

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .setValue(GuaranteeReferenceNumberPage, Gen.alphaNumStr.sample.value)
    .setValue(AccessCodePage, Gen.alphaNumStr.sample.value)
    .setValue(EoriNumberPage, Gen.alphaNumStr.sample.value)

  private val mockViewModelProvider: CheckYourAnswersViewModelProviderV2 = mock[CheckYourAnswersViewModelProviderV2]

  override protected def applicationBuilder(): GuiceApplicationBuilder =
    super
      .applicationBuilder()
      .overrides(bind[CheckYourAnswersViewModelProviderV2].toInstance(mockViewModelProvider))

  private val section: Section = arbitrary[Section].sample.value

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockViewModelProvider)
    when(mockViewModelProvider.apply(any())(any())).thenReturn(CheckYourAnswersViewModelV2(section))
  }

  "CheckYourAnswers Controller" - {

    "return OK and the correct view for a GET" in {
      val userAnswers = baseAnswers
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, routes.CheckYourAnswersControllerV2.onPageLoad().url)
      val view    = injector.instanceOf[CheckYourAnswersViewV2]
      val result  = route(app, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(Seq(section))(request, messages).toString

      verify(mockViewModelProvider)(userAnswers)
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {
      setNoExistingUserAnswers()

      val request = FakeRequest(GET, routes.CheckYourAnswersControllerV2.onPageLoad().url)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must pass the response from the submit onto the processor" in {
      val userAnswers = baseAnswers
      setExistingUserAnswers(userAnswers)

      when(mockGuaranteeBalanceService.retrieveBalanceResponse()(any(), any()))
        .thenReturn(Future.successful(Right(BalanceRequestSuccess(123.45, Some(CurrencyCode("GBP"))))))

      val request = FakeRequest(POST, routes.CheckYourAnswersControllerV2.onSubmit().url)
      val result  = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url

      verify(mockGuaranteeBalanceService).retrieveBalanceResponse()(any(), any())
    }
  }
}
