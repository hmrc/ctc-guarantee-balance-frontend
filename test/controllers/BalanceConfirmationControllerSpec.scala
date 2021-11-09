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
import matchers.JsonMatchers.containJson
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.{BalancePage, EoriNumberPage}
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._

class BalanceConfirmationControllerSpec extends SpecBase with MockitoSugar with AppWithDefaultMockFixtures {

  "BalanceConfirmation Controller" - {

    // format: off
    ".onPageLoad" - {
      "must return OK and the correct view for a GET" - {

        "when balance found in user answers" in {

          val balance = "£8,500.00"

          forAll(arbitrary[Boolean]) {
            isEnrolled =>
              beforeEach()

              val userAnswers = emptyUserAnswers
                .set(BalancePage, balance).success.value

              val application                            = applicationBuilder(userAnswers = Some(userAnswers), isEnrolled).build()
              val request                                = FakeRequest(GET, routes.BalanceConfirmationController.onPageLoad().url)
              val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
              val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

              val result = route(application, request).value

              status(result) mustEqual OK

              verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

              val expectedJson = Json.obj(
                "balance"                         -> balance,
                "isEnrolled"                        -> isEnrolled,
                "checkAnotherGuaranteeBalanceUrl" -> routes.BalanceConfirmationController.checkAnotherGuaranteeBalance().url
              )

              templateCaptor.getValue mustEqual "balanceConfirmation.njk"
              jsonCaptor.getValue must containJson(expectedJson)

              application.stop()
          }
        }
      }

      "must redirect to start controller" - {
        "when balance not found in user answers" in {

          forAll(arbitrary[Boolean]) {
            isEnrolled =>
              beforeEach()

              val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), isEnrolled).build()
              val request     = FakeRequest(GET, routes.BalanceConfirmationController.onPageLoad().url)

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).value mustEqual routes.StartController.start().url

              application.stop()
          }
        }
      }
    }
    // format: on

    ".checkAnotherGuaranteeBalance" - {
      "must clear user answers and redirect to EORI Number page" in {

        val userAnswers = emptyUserAnswers.set(EoriNumberPage, "eori").success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
        val request     = FakeRequest(GET, routes.BalanceConfirmationController.checkAnotherGuaranteeBalance().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.EoriNumberController.onPageLoad(NormalMode).url

        val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(uaCaptor.capture)
        uaCaptor.getValue.data mustEqual Json.obj()

        application.stop()
      }
    }

    ".manageTransitMovements" - {
      "must clear user answers and redirect to manage transit movements" in {

        val userAnswers = emptyUserAnswers.set(EoriNumberPage, "eori").success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()
        val request     = FakeRequest(GET, routes.BalanceConfirmationController.manageTransitMovements().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual frontendAppConfig.manageTransitMovementsUrl

        val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
        verify(mockSessionRepository).set(uaCaptor.capture)
        uaCaptor.getValue.data mustEqual Json.obj()

        application.stop()
      }
    }

  }
}
