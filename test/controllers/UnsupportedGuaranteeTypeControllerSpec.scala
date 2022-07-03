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
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.UnsupportedGuaranteeTypeView

class UnsupportedGuaranteeTypeControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  "UnsupportedGuaranteeType Controller" - {

    "onPageLoad" - {
      "return OK and the correct view for a GET" in {
        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, routes.UnsupportedGuaranteeTypeController.onPageLoad().url)
        val view    = injector.instanceOf[UnsupportedGuaranteeTypeView]
        val result  = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view()(request, messages).toString
      }
    }

    "onSubmit" - {
      "must Redirect to the StartController DataReturned " in {
        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(POST, routes.UnsupportedGuaranteeTypeController.onSubmit().url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.StartController.startAgain().url
      }
    }
  }
}
