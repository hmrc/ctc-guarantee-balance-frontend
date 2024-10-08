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
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import views.html.TryAgainView

class TryAgainControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  override protected def applicationBuilder(): GuiceApplicationBuilder =
    super.applicationBuilder()

  implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  "TryAgainController" - {

    "onLoad" - {
      "must return OK and the correct view for a GET" - {
        setExistingUserAnswers(emptyUserAnswers)

        val request = FakeRequest(GET, routes.TryAgainController.onPageLoad().url)
        val view    = injector.instanceOf[TryAgainView]
        val result  = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(None)(request, messages).toString
      }
    }
  }
}
