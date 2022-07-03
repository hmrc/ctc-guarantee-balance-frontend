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
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._

class DetailsDontMatchControllerSpec extends SpecBase with MockitoSugar with AppWithDefaultMockFixtures {

  "DetailsDontMatch Controller" - {

    "return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)
      val request                                = FakeRequest(GET, routes.DetailsDontMatchController.onPageLoad().url)
      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

      val result = route(app, request).value

      status(result) mustEqual OK

      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      templateCaptor.getValue mustEqual "detailsDontMatch.njk"
    }
  }
}
