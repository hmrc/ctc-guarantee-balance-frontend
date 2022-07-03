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
import forms.EoriNumberFormProvider
import matchers.JsonMatchers
import models.{Mode, NormalMode, UserAnswers}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.EoriNumberPage
import play.api.inject.bind
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.viewmodels.NunjucksSupport

class EoriNumberControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers with AppWithDefaultMockFixtures {

  val formProvider = new EoriNumberFormProvider()
  val form         = formProvider()

  def eoriNumberRoute(mode: Mode = NormalMode): String = routes.EoriNumberController.onPageLoad(mode).url

  "EoriNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      forAll(arbitrary[Mode]) {
        mode =>
          beforeEach()

          val application                            = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
          val request                                = FakeRequest(GET, eoriNumberRoute(mode))
          val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
          val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

          val result = route(application, request).value

          status(result) mustEqual OK

          verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

          val expectedJson = Json.obj(
            "form" -> form,
            "mode" -> mode
          )

          templateCaptor.getValue mustEqual "eoriNumber.njk"
          jsonCaptor.getValue must containJson(expectedJson)

          application.stop()
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      forAll(arbitrary[Mode]) {
        mode =>
          beforeEach()

          val userAnswers = UserAnswers(userAnswersId).set(EoriNumberPage, validEori).success.value

          val application                            = applicationBuilder(userAnswers = Some(userAnswers)).build()
          val request                                = FakeRequest(GET, eoriNumberRoute(mode))
          val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
          val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

          val result = route(application, request).value

          status(result) mustEqual OK

          verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

          val filledForm = form.bind(Map("value" -> validEori))

          val expectedJson = Json.obj(
            "form" -> filledForm,
            "mode" -> mode
          )

          templateCaptor.getValue mustEqual "eoriNumber.njk"
          jsonCaptor.getValue must containJson(expectedJson)

          application.stop()
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute))
          )
          .build()

      val request =
        FakeRequest(POST, eoriNumberRoute())
          .withFormUrlEncodedBody(("value", validEori))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
      verify(mockSessionRepository).set(uaCaptor.capture)
      uaCaptor.getValue.get(EoriNumberPage).get mustBe validEori

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      forAll(arbitrary[Mode]) {
        mode =>
          beforeEach()

          val application                            = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
          val request                                = FakeRequest(POST, eoriNumberRoute(mode)).withFormUrlEncodedBody(("value", ""))
          val boundForm                              = form.bind(Map("value" -> ""))
          val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
          val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

          val result = route(application, request).value

          status(result) mustEqual BAD_REQUEST

          verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

          val expectedJson = Json.obj(
            "form" -> boundForm,
            "mode" -> mode
          )

          templateCaptor.getValue mustEqual "eoriNumber.njk"
          jsonCaptor.getValue must containJson(expectedJson)

          application.stop()
      }
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, eoriNumberRoute())

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request =
        FakeRequest(POST, eoriNumberRoute())
          .withFormUrlEncodedBody(("value", validEori))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

  }
}
