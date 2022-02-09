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
import config.FrontendAppConfig
import matchers.JsonMatchers
import models.{PollMode, SubmitMode, UserAnswers}
import models.backend.BalanceRequestSuccess
import models.values.{BalanceId, CurrencyCode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import pages.{AccessCodePage, BalanceIdPage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class WaitOnGuaranteeBalanceControllerSpec extends SpecBase with JsonMatchers with AppWithDefaultMockFixtures {

  private val expectedUuid: UUID   = UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4")
  private val balanceId: BalanceId = BalanceId(expectedUuid)

  private val grn: String    = "grn"
  private val access: String = "access"
  private val taxId: String  = "taxId"

  // format: off
  private val baseAnswers: UserAnswers = emptyUserAnswers
    .set(GuaranteeReferenceNumberPage, grn).success.value
    .set(AccessCodePage, access).success.value
    .set(EoriNumberPage, taxId).success.value

  private val baseAnswersWithBalanceId: UserAnswers = baseAnswers
    .set(BalanceIdPage, balanceId).success.value
  // format: on

  private val successResponse = Right(BalanceRequestSuccess(BigDecimal(99.9), CurrencyCode("GBP")))
  private val errorResponse   = Left(HttpResponse(404, ""))

  implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  "WaitOnGuaranteeBalanceController" - {

    "onLoad" - {
      "must return OK and the correct view for a GET" in {
        val request = FakeRequest(GET, routes.WaitOnGuaranteeBalanceController.onPageLoad(BalanceId(expectedUuid), PollMode).url)

        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()
        val result      = route(application, request).value

        status(result) mustEqual OK

        val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val config = application.injector.instanceOf[FrontendAppConfig]
        val expectedJson = Json.obj(
          "balanceId"         -> balanceId,
          "waitTimeInSeconds" -> config.guaranteeBalanceDisplayDelay
        )

        jsonCaptor.getValue must containJson(expectedJson)
        templateCaptor.getValue mustBe "waitOnGuaranteeBalance.njk"
      }
    }

    "checkDetails" - {
      "must Redirect to Check Your Answers and update user answers if Poll Mode" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))

        val request     = FakeRequest(GET, routes.WaitOnGuaranteeBalanceController.checkDetails(balanceId, PollMode).url)
        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()
        val result      = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url

        val userAnswersCapture: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository, times(1)).set(userAnswersCapture.capture())
        userAnswersCapture.getValue.get(BalanceIdPage).isDefined mustEqual true
        userAnswersCapture.getValue.get(BalanceIdPage) mustEqual Some(balanceId)
      }

      "must Redirect to Check Your Answers and remove the balance id from userAnswers if Submit Mode" in {

        val request     = FakeRequest(GET, routes.WaitOnGuaranteeBalanceController.checkDetails(balanceId, SubmitMode).url)
        val application = applicationBuilder(userAnswers = Some(baseAnswersWithBalanceId)).build()
        val result      = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url

        val userAnswersCapture: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository, times(1)).set(userAnswersCapture.capture())
        userAnswersCapture.getValue.get(BalanceIdPage).isDefined mustEqual false
      }

      "must Redirect to Check Your Answers and ignore userAnswers if Submit Mode and we don't have a balanceIdPage set" in {

        val request     = FakeRequest(GET, routes.WaitOnGuaranteeBalanceController.checkDetails(balanceId, SubmitMode).url)
        val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()
        val result      = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url

        val userAnswersCapture: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository, times(1)).set(userAnswersCapture.capture())
        userAnswersCapture.getValue.get(BalanceIdPage).isDefined mustEqual false
      }
    }

    "onSubmit" - {
      "must poll for a response and then Redirect to the Balance Confirmation Controller if the status is DataReturned and Poll Mode" in {
        when(mockGuaranteeBalanceService.pollForGuaranteeBalance(eqTo(balanceId))(any(), any())).thenReturn(Future.successful(successResponse))

        val request     = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId, PollMode).url)
        val application = applicationBuilder(userAnswers = Some(baseAnswersWithBalanceId)).build()
        val result      = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url
        verify(mockGuaranteeBalanceService, times(1)).pollForGuaranteeBalance(eqTo(balanceId))(any(), any())

      }

      "must submit a request and then Redirect to the Balance Confirmation Controller if the status is DataReturned and Submit Mode" in {
        when(mockGuaranteeBalanceService.submitBalanceRequest()(any(), any())).thenReturn(Future.successful(successResponse))

        val request     = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId, SubmitMode).url)
        val application = applicationBuilder(userAnswers = Some(baseAnswersWithBalanceId)).build()
        val result      = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url
        verify(mockGuaranteeBalanceService, times(1)).submitBalanceRequest()(any(), any())
      }

      "must show the technical difficulties page if we have an error " in {
        when(mockGuaranteeBalanceService.pollForGuaranteeBalance(eqTo(balanceId))(any(), any())).thenReturn(Future.successful(errorResponse))

        val request = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId, PollMode).url)

        val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        val application                            = applicationBuilder(userAnswers = Some(baseAnswersWithBalanceId)).build()
        val result                                 = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())
        templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      }
    }
  }
}
