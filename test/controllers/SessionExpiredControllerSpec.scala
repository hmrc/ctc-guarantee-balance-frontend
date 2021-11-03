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
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify}
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._

class SessionExpiredControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  "must return OK and the correct view for a GET" in {

    val request = FakeRequest(GET, routes.SessionExpiredController.onPageLoad().url)

    val result = route(app, request).value

    status(result) mustEqual OK

    val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
    val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

    verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

    val expectedJson = Json.obj(
      "signInUrl" -> "http://localhost:9485/manage-transit-movements/what-do-you-want-to-do"
    )

    templateCaptor.getValue mustEqual "session-expired.njk"
    jsonCaptor.getValue must containJson(expectedJson)
  }
}
