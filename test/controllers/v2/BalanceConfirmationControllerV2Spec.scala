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
import models.{Referral, Timestamp, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.{BalancePage, EoriNumberPage}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DateTimeService
import views.html.v2.BalanceConfirmationViewV2

class BalanceConfirmationControllerV2Spec extends SpecBase with MockitoSugar with AppWithDefaultMockFixtures {

  private val mockDateTimeService: DateTimeService = mock[DateTimeService]

  override protected def applicationBuilder(): GuiceApplicationBuilder =
    super
      .v2ApplicationBuilder()
      .overrides(bind[DateTimeService].toInstance(mockDateTimeService))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDateTimeService)
  }

  "BalanceConfirmation Controller" - {

    ".onPageLoad" - {
      "must return OK and the correct view for a GET" - {

        "when balance found in user answers" - {

          val balance = "£8,500.00"

          "when session has referral value" in {

            forAll(arbitrary[Timestamp], arbitrary[Referral]) {
              (timestamp, referral) =>
                beforeEach()

                when(mockDateTimeService.timestamp).thenReturn(timestamp)

                val userAnswers = emptyUserAnswers.setValue(BalancePage, balance)
                setExistingUserAnswers(userAnswers)

                val request = FakeRequest(GET, routes.BalanceConfirmationController.onPageLoad().url)
                  .withSession(Referral.key -> referral.toString)
                val view   = injector.instanceOf[BalanceConfirmationViewV2]
                val result = route(app, request).value

                status(result) mustEqual OK

                contentAsString(result) mustEqual
                  view(balance, timestamp, Some(referral.toString))(request, messages).toString

                verify(mockDateTimeService).timestamp
            }
          }

          "when session does not have referral value" in {
            forAll(arbitrary[Timestamp]) {
              timestamp =>
                beforeEach()

                when(mockDateTimeService.timestamp).thenReturn(timestamp)

                val userAnswers = emptyUserAnswers.setValue(BalancePage, balance)

                setExistingUserAnswers(userAnswers)

                val request = FakeRequest(GET, routes.BalanceConfirmationController.onPageLoad().url)
                val view    = injector.instanceOf[BalanceConfirmationViewV2]
                val result  = route(app, request).value

                status(result) mustEqual OK

                contentAsString(result) mustEqual
                  view(balance, timestamp, None)(request, messages).toString
            }
          }
        }
      }

      "must redirect to session expired" - {
        "when balance not found in user answers" in {

          setExistingUserAnswers(emptyUserAnswers)
          val request = FakeRequest(GET, routes.BalanceConfirmationController.onPageLoad().url)

          val result = route(app, request).value

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
        }
      }
    }

    ".checkAnotherGuaranteeBalance" - {
      "must clear user answers and redirect to EORI Number page" in {

        val userAnswers = emptyUserAnswers.setValue(EoriNumberPage, "eori")

        setExistingUserAnswers(userAnswers)
        val request = FakeRequest(GET, routes.BalanceConfirmationController.checkAnotherGuaranteeBalance().url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.StartController.startAgain().url

        val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(uaCaptor.capture)
        uaCaptor.getValue.data mustEqual Json.obj()
      }
    }

    ".manageTransitMovements" - {
      "must clear user answers and redirect to manage transit movements" in {

        val userAnswers = emptyUserAnswers.setValue(EoriNumberPage, "eori")

        setExistingUserAnswers(userAnswers)
        val request = FakeRequest(GET, routes.BalanceConfirmationController.manageTransitMovements().url)

        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.manageTransitMovementsUrl

        val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(uaCaptor.capture)
        uaCaptor.getValue.data mustEqual Json.obj()
      }
    }

  }
}
