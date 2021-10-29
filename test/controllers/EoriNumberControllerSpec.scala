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
import forms.EoriNumberFormProvider
import matchers.JsonMatchers
import models.{Mode, NormalMode, Referral, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.{EoriNumberPage, ReferralPage}
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.viewmodels.NunjucksSupport

import java.time.LocalDateTime

class EoriNumberControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers with AppWithDefaultMockFixtures {

  def onwardRoute = Call("GET", "/foo")

  val formProvider = new EoriNumberFormProvider()
  val form         = formProvider()

  val validAnswer: String = "GB123"

  def eoriNumberRoute(mode: Mode = NormalMode, referral: Referral): String = routes.EoriNumberController.onPageLoad(mode, referral).url

  "EoriNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      forAll(arbitrary[Mode], arbitrary[Referral]) {
        (mode, referral) =>
          beforeEach()

          val application    = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
          val request        = FakeRequest(GET, eoriNumberRoute(mode, referral))
          val templateCaptor = ArgumentCaptor.forClass(classOf[String])
          val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

          val result = route(application, request).value

          status(result) mustEqual OK

          verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

          val expectedJson = Json.obj(
            "form"     -> form,
            "mode"     -> mode,
            "referral" -> referral
          )

          templateCaptor.getValue mustEqual "eoriNumber.njk"
          jsonCaptor.getValue must containJson(expectedJson)

          application.stop()
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      forAll(arbitrary[Mode], arbitrary[Referral]) {
        (mode, referral) =>
          beforeEach()

          val userAnswers = UserAnswers(userAnswersId).set(EoriNumberPage, validAnswer).success.value

          val application    = applicationBuilder(userAnswers = Some(userAnswers)).build()
          val request        = FakeRequest(GET, eoriNumberRoute(mode, referral))
          val templateCaptor = ArgumentCaptor.forClass(classOf[String])
          val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

          val result = route(application, request).value

          status(result) mustEqual OK

          verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

          val filledForm = form.bind(Map("value" -> validAnswer))

          val expectedJson = Json.obj(
            "form"     -> filledForm,
            "mode"     -> mode,
            "referral" -> referral
          )

          templateCaptor.getValue mustEqual "eoriNumber.njk"
          jsonCaptor.getValue must containJson(expectedJson)

          application.stop()
      }
    }

    "must redirect to the next page when valid data is submitted and there are existing user answers" in {

      forAll(arbitrary[Referral]) {
        referral =>
          beforeEach()

          val time = LocalDateTime.now()

          val application =
            applicationBuilder(userAnswers = Some(emptyUserAnswers.copy(lastUpdated = time)))
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
              )
              .build()

          val request =
            FakeRequest(POST, eoriNumberRoute(referral = referral))
              .withFormUrlEncodedBody(("value", validAnswer))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(uaCaptor.capture)

          uaCaptor.getValue.lastUpdated mustBe time // check that user answers have been persisted
          uaCaptor.getValue.get(EoriNumberPage).get mustBe validAnswer
          uaCaptor.getValue.get(ReferralPage).get mustBe referral

          application.stop()
      }
    }

    "must redirect to the next page when valid data is submitted and there are no existing user answers" in {

      forAll(arbitrary[Referral]) {
        referral =>
          beforeEach()

          val time = LocalDateTime.now()

          val application =
            applicationBuilder(userAnswers = None)
              .overrides(
                bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
              )
              .build()

          val request =
            FakeRequest(POST, eoriNumberRoute(referral = referral))
              .withFormUrlEncodedBody(("value", validAnswer))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual onwardRoute.url

          val uaCaptor = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(uaCaptor.capture)

          uaCaptor.getValue.lastUpdated.isAfter(time) mustBe true // check that new user answers have been created
          uaCaptor.getValue.get(EoriNumberPage).get mustBe validAnswer
          uaCaptor.getValue.get(ReferralPage).get mustBe referral

          application.stop()
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      forAll(arbitrary[Mode], arbitrary[Referral]) {
        (mode, referral) =>
          beforeEach()

          val application    = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
          val request        = FakeRequest(POST, eoriNumberRoute(mode, referral)).withFormUrlEncodedBody(("value", ""))
          val boundForm      = form.bind(Map("value" -> ""))
          val templateCaptor = ArgumentCaptor.forClass(classOf[String])
          val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

          val expectedJson = Json.obj(
            "form"     -> boundForm,
            "mode"     -> mode,
            "referral" -> referral
          )

          templateCaptor.getValue mustEqual "eoriNumber.njk"
          jsonCaptor.getValue must containJson(expectedJson)

          application.stop()
      }
    }

  }
}
