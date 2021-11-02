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
import models.backend.{BalanceRequestNotMatched, BalanceRequestPending, BalanceRequestPendingExpired, BalanceRequestSuccess}
import models.values.{BalanceId, CurrencyCode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse}
import java.util.UUID

import config.FrontendAppConfig
import matchers.JsonMatchers
import pages.GuaranteeReferenceNumberPage
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.Future

class WaitOnGuaranteeBalanceControllerSpec extends SpecBase with JsonMatchers with AppWithDefaultMockFixtures {

  val expectedUuid = UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4")
  val balanceId    = BalanceId(expectedUuid)

  private val grn: String  = "grn"
  val populatedUserAnswers = emptyUserAnswers.set(GuaranteeReferenceNumberPage, grn).success.value

  val noMatchResponse  = Right(BalanceRequestNotMatched)
  val successResponse  = Right(BalanceRequestSuccess(BigDecimal(99.9), CurrencyCode("GBP")))
  val pendingResponse  = Right(BalanceRequestPending(balanceId))
  val tryAgainResponse = Right(BalanceRequestPendingExpired(balanceId))
  val errorResponse    = Left(HttpResponse(404, ""))

  implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  "WaitOnGuaranteeBalanceController" - {

    "onLoad" - {
      "must return OK and the correct view for a GET" in {
        val request = FakeRequest(GET, routes.WaitOnGuaranteeBalanceController.onPageLoad(BalanceId(expectedUuid)).url)

        val application = applicationBuilder(userAnswers = Some(populatedUserAnswers)).build()
        val result      = route(application, request).value

        status(result) mustEqual OK

        val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val config = application.injector.instanceOf[FrontendAppConfig]
        val expectedJson = Json.obj(
          "balanceId"         -> balanceId,
          "waitTimeInSeconds" -> config.guaranteeBalanceMaxTimeInSecond
        )

        jsonCaptor.getValue must containJson(expectedJson)
        templateCaptor.getValue mustBe "waitOnGuaranteeBalance.njk"
      }
    }

    "onSubmit" - {
      "must Redirect to the TryAgain Controller if the status is empty " in {
        when(mockGuaranteeBalanceService.pollForGuaranteeBalance(eqTo(balanceId), any(), any())(any())).thenReturn(Future.successful(tryAgainResponse))

        val request = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId).url)

        val application = applicationBuilder(userAnswers = Some(populatedUserAnswers)).build()
        val result      = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.TryGuaranteeBalanceAgainController.onPageLoad(balanceId).url
      }

      "must Redirect to the DetailsDontMatchController if the status is NoMatch " in {
        when(mockGuaranteeBalanceService.pollForGuaranteeBalance(eqTo(balanceId), any(), any())(any())).thenReturn(Future.successful(noMatchResponse))

        val request     = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId).url)
        val application = applicationBuilder(userAnswers = Some(populatedUserAnswers)).build()
        val result      = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.DetailsDontMatchController.onPageLoad().url
      }

      "must return back to the wait page if the status is still pending " in {
        when(mockGuaranteeBalanceService.pollForGuaranteeBalance(eqTo(balanceId), any(), any())(any())).thenReturn(Future.successful(pendingResponse))

        val request = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId).url)

        val templateCaptor                       = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor: ArgumentCaptor[JsObject] = ArgumentCaptor.forClass(classOf[JsObject])
        val application                          = applicationBuilder(userAnswers = Some(populatedUserAnswers)).build()
        val result                               = route(application, request).value

        status(result) mustEqual OK

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val config = application.injector.instanceOf[FrontendAppConfig]

        val expectedJson = Json.obj(
          "balanceId"         -> balanceId,
          "waitTimeInSeconds" -> config.guaranteeBalanceMaxTimeInSecond
        )

        jsonCaptor.getValue must containJson(expectedJson)
        templateCaptor.getValue mustEqual "waitOnGuaranteeBalance.njk"
      }

      "must Redirect to the Balance Confirmation Controller if the status is DataReturned " in {
        when(mockGuaranteeBalanceService.pollForGuaranteeBalance(eqTo(balanceId), any(), any())(any())).thenReturn(Future.successful(successResponse))

        val request     = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId).url)
        val application = applicationBuilder(userAnswers = Some(populatedUserAnswers)).build()
        val result      = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url
      }

      "must show the technical difficulties page if we have an error " in {
        when(mockGuaranteeBalanceService.pollForGuaranteeBalance(eqTo(balanceId), any(), any())(any())).thenReturn(Future.successful(errorResponse))

        val request = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId).url)

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val application    = applicationBuilder(userAnswers = Some(populatedUserAnswers)).build()
        val result         = route(application, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())
        templateCaptor.getValue mustEqual "technicalDifficulties.njk"
      }
    }
  }
}
