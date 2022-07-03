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
import models.values.CurrencyCode
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{AccessCodePage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewModels.{CheckYourAnswersViewModel, CheckYourAnswersViewModelProvider, Section}

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with AppWithDefaultMockFixtures {

  private val grn: String    = "grn"
  private val access: String = "access"
  private val taxId: String  = "taxId"

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .setValue(GuaranteeReferenceNumberPage, grn)
    .setValue(AccessCodePage, access)
    .setValue(EoriNumberPage, taxId)

  private val mockViewModelProvider: CheckYourAnswersViewModelProvider = mock[CheckYourAnswersViewModelProvider]

  override protected def applicationBuilder(): GuiceApplicationBuilder =
    super
      .applicationBuilder()
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
      setExistingUserAnswers(userAnswers)
      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

      val expectedJson = Json.obj(
        "section" -> emptySection
      )

      templateCaptor.getValue mustEqual "checkYourAnswers.njk"
      jsonCaptor.getValue must containJson(expectedJson)

      verify(mockViewModelProvider)(userAnswers)
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()
      val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must pass the response from the submit onto the processor" in {

      val userAnswers = baseAnswers
      setExistingUserAnswers(userAnswers)
      val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)

      when(mockGuaranteeBalanceService.retrieveBalanceResponse()(any(), any()))
        .thenReturn(Future.successful(Right(BalanceRequestSuccess(123.45, CurrencyCode("GBP")))))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url

      verify(mockGuaranteeBalanceService).retrieveBalanceResponse()(any(), any())
    }
  }
}
