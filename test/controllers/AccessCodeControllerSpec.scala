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
import forms.AccessCodeFormProvider
import models.NormalMode
import navigation.Navigator
import pages.AccessCodePage
import play.api.data.Form
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.AccessCodeView

class AccessCodeControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val formProvider                 = new AccessCodeFormProvider()
  private val form: Form[String]           = formProvider()
  private val mode                         = NormalMode
  private lazy val accessCodeRoute: String = routes.AccessCodeController.onPageLoad(mode).url

  private val validAnswer: String = "1111"
  private val invalidAnswer       = ""

  "AccessCode Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(GET, accessCodeRoute)
      val view    = injector.instanceOf[AccessCodeView]
      val result  = route(application, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mode)(request, messages).toString

      application.stop()
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(AccessCodePage, validAnswer)
      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      val request = FakeRequest(GET, accessCodeRoute)
      val view    = injector.instanceOf[AccessCodeView]
      val result  = route(application, request).value

      status(result) mustEqual OK

      val filledForm = form.bind(Map("value" -> validAnswer))

      contentAsString(result) mustEqual
        view(filledForm, mode)(request, messages).toString

      application.stop()
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[Navigator].toInstance(fakeNavigator))
        .build()

      val request = FakeRequest(POST, accessCodeRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url

      application.stop()
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      val request = FakeRequest(POST, accessCodeRoute).withFormUrlEncodedBody(("value", invalidAnswer))
      val view    = injector.instanceOf[AccessCodeView]
      val result  = route(application, request).value

      status(result) mustEqual BAD_REQUEST

      val boundForm = form.bind(Map("value" -> invalidAnswer))

      contentAsString(result) mustEqual
        view(boundForm, mode)(request, messages).toString

      application.stop()
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(GET, accessCodeRoute)

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      val request = FakeRequest(POST, accessCodeRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(application, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

      application.stop()
    }
  }
}
