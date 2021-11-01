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
import models.{BalanceId, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.twirl.api.Html
import services.{BalanceStatus, GuaranteeBalanceService}

import scala.concurrent.Future

class WaitOnGuaranteeBalanceControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  val balanceId = BalanceId("1")

  "WaitOnGuaranteeBalanceController" - {

    "onLoad" - {
      "must return OK and the correct view for a GET" in {

        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        val request = FakeRequest(GET, routes.WaitOnGuaranteeBalanceController.onPageLoad(BalanceId("1")).url)

        val result = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

        templateCaptor.getValue mustEqual "waitOnGuaranteeBalance.njk"
      }
    }

    "onSubmit" - {
      "must Redirect to the TryAgain Controller if the status is empty " in {
        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[GuaranteeBalanceService].toInstance(new TestGuaranteeBalanceService(Future.successful(None)))
          )
          .build()

        val request = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.TryGuaranteeBalanceAgainController.onPageLoad(balanceId).url
      }

      "must Redirect to the DetailsDontMatchController if the status is NoMatch " in {
        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[GuaranteeBalanceService].toInstance(new TestGuaranteeBalanceService(Future.successful(Some(BalanceStatus.NoMatch))))
          )
          .build()

        val request = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.DetailsDontMatchController.onPageLoad().url
      }

      "must return back to the wait page if the status is still pending " in {
        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[GuaranteeBalanceService].toInstance(new TestGuaranteeBalanceService(Future.successful(Some(BalanceStatus.PendingStatus))))
          )
          .build()
        val request = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId).url)

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val result         = route(application, request).value

        status(result) mustEqual OK

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())
        templateCaptor.getValue mustEqual "waitOnGuaranteeBalance.njk"
      }

      "must Redirect to the Balance Confirmation Controller if the status is DataReturned " in {
        when(mockRenderer.render(any(), any())(any())).thenReturn(Future.successful(Html("")))

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[GuaranteeBalanceService].toInstance(new TestGuaranteeBalanceService(Future.successful(Some(BalanceStatus.DataReturned))))
          )
          .build()

        val request = FakeRequest(POST, routes.WaitOnGuaranteeBalanceController.onSubmit(balanceId).url)
        val result  = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url
      }
    }
  }

  class TestGuaranteeBalanceService(result: Future[Option[BalanceStatus]]) extends GuaranteeBalanceService {
    override def getGuaranteeBalance(balanceId: BalanceId): Future[Option[BalanceStatus]] = result
  }
}
