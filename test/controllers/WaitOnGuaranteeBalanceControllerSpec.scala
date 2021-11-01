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

import java.util.UUID

import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptyList
import models.values.{BalanceId, CurrencyCode, ErrorType}
import models.backend.{errors, BalanceRequestFunctionalError, BalanceRequestPending, BalanceRequestSuccess}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.GuaranteeBalanceService
import uk.gov.hmrc.http.HttpResponse

import scala.concurrent.Future

class WaitOnGuaranteeBalanceControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  val expectedUuid = UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4")
  val balanceId    = BalanceId(expectedUuid)

  val noMatchResponse = Right(BalanceRequestFunctionalError(NonEmptyList(errors.FunctionalError(ErrorType(1), "", None), Nil)))
  val successResponse = Right(BalanceRequestSuccess(BigDecimal(99.9), CurrencyCode("GBP")))
  val pendingResponse = Right(BalanceRequestPending(balanceId))
  val errorResponse   = Left(HttpResponse(404, ""))

  "WaitOnGuaranteeBalanceController" - {

    "onLoad" - {
      "must return OK and the correct view for a GET" in {
        val request = FakeRequest(GET, routes.WaitOnGuaranteeBalanceController.onPageLoad(BalanceId(expectedUuid)).url)

        val result = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

        templateCaptor.getValue mustBe "waitOnGuaranteeBalance.njk"
      }
    }

    "onSubmit" - {
      "must Redirect to the TryAgain Controller if the status is empty " in {
        when(mockGuaranteeBalanceService.pollForGuaranteeBalance(eqTo(balanceId), any(), any())).thenReturn(Future.successful(errorResponse))

        val request = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId).url)

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
        val result      = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.TryGuaranteeBalanceAgainController.onPageLoad(balanceId).url
      }

//      "must Redirect to the DetailsDontMatchController if the status is NoMatch " in {
//        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
//        when(mockGuaranteeBalanceService.pollForGuaranteeBalance(eqTo(balanceId), any(), any())).thenReturn(Future.successful(noMatchResponse))
//
//        val request = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId).url)
//        val result  = route(app, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual routes.DetailsDontMatchController.onPageLoad().url
//      }

//      "must return back to the wait page if the status is still pending " in {
//        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
//        when(mockGuaranteeBalanceService.pollForGuaranteeBalance(eqTo(balanceId), any(), any())).thenReturn(Future.successful(pendingResponse))
//
//        val request = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId).url)
//
//        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
//        val result         = route(app, request).value
//
//        status(result) mustEqual OK
//
//        verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())
//        templateCaptor.getValue mustEqual "waitOnGuaranteeBalance.njk"
//      }
//
//      "must Redirect to the Balance Confirmation Controller if the status is DataReturned " in {
//        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))
//        when(mockGuaranteeBalanceService.pollForGuaranteeBalance(eqTo(balanceId), any(), any())).thenReturn(Future.successful(successResponse))
//
//        val request = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId).url)
//        val result  = route(app, request).value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url
//      }

    }
  }
}
