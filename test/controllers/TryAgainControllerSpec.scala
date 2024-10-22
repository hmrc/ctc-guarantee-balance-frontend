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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import handlers.GuaranteeBalanceResponseHandler
import models.backend.BalanceRequestSuccess
import models.values.BalanceId
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{reset, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.BalanceIdPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.GuaranteeBalanceService
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import views.html.TryAgainView

import java.util.UUID
import scala.concurrent.Future

class TryAgainControllerSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks {

  private val mockGuaranteeBalanceResponseHandler: GuaranteeBalanceResponseHandler = mock[GuaranteeBalanceResponseHandler]

  override protected def applicationBuilder(): GuiceApplicationBuilder =
    super
      .applicationBuilder()
      .bindings(
        bind[GuaranteeBalanceResponseHandler].toInstance(mockGuaranteeBalanceResponseHandler)
      )

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockGuaranteeBalanceService)
    reset(mockGuaranteeBalanceResponseHandler)
  }

  implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  "TryAgainController" - {

    "onLoad" - {
      "must return OK and the correct view for a GET" - {
        "when BalanceIdPage is unpopulated" in {
          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(GET, routes.TryAgainController.onPageLoad().url)
          val view    = injector.instanceOf[TryAgainView]
          val result  = route(app, request).value

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(None)(request, messages).toString
        }

        "when BalanceIdPage is populated" in {
          forAll(arbitrary[UUID]) {
            uuid =>
              val userAnswers = emptyUserAnswers.setValue(BalanceIdPage, BalanceId(uuid))
              setExistingUserAnswers(userAnswers)

              val request = FakeRequest(GET, routes.TryAgainController.onPageLoad().url)
              val view    = injector.instanceOf[TryAgainView]
              val result  = route(app, request).value

              status(result) mustEqual OK

              contentAsString(result) mustEqual
                view(Some(uuid))(request, messages).toString
          }
        }
      }
    }

    "onSubmit" - {
      "must handle response" - {
        "when success" in {
          val response = Right(BalanceRequestSuccess(100, None))

          val redirectUrl = controllers.routes.BalanceConfirmationController.onPageLoad()

          when(mockGuaranteeBalanceService.submitBalanceRequest()(any(), any()))
            .thenReturn(Future.successful(response))

          when(mockGuaranteeBalanceResponseHandler.processResponse(eqTo(response))(any(), any()))
            .thenReturn(Future.successful(Redirect(redirectUrl)))

          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(POST, routes.TryAgainController.onSubmit().url)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual redirectUrl.url
        }
      }
    }
  }
}
