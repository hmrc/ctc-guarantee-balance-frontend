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

package renderer

import base.{AppWithDefaultMockFixtures, SpecBase}
import org.mockito.ArgumentCaptor
import org.mockito.Matchers.any
import org.mockito.Mockito.{times, verify, when}
import play.api.libs.json._
import play.api.test.FakeRequest
import play.twirl.api.Html

import scala.concurrent.Future

class RendererSpec extends SpecBase with AppWithDefaultMockFixtures {

  implicit private val request: FakeRequest[_] = FakeRequest()

  "render" - {

    "must pass config values to the Nunjucks Renderer" - {

      "when called with only a template" in {

        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        val renderer = app.injector.instanceOf[Renderer]

        renderer.render("foo").futureValue

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val json = jsonCaptor.getValue

        (json \ "config") mustBe a[JsDefined]
      }
    }

    "must pass config values to the Nunjucks Renderer" - {

      "when called with a template and a JsObject" in {

        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        val renderer = app.injector.instanceOf[Renderer]

        renderer.render("foo", Json.obj("bar" -> "baz")).futureValue

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val json = jsonCaptor.getValue

        (json \ "config") mustBe a[JsDefined]
      }
    }

    "must pass config values to the Nunjucks Renderer" - {

      "when called with a template and a writable object" in {

        when(mockRenderer.render(any(), any())(any()))
          .thenReturn(Future.successful(Html("")))

        val templateCaptor = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor     = ArgumentCaptor.forClass(classOf[JsObject])

        val renderer = app.injector.instanceOf[Renderer]

        renderer.render("foo", TestClassWithWrites("bar")).futureValue

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val json = jsonCaptor.getValue

        (json \ "config") mustBe a[JsDefined]
      }
    }
  }
}

case class TestClassWithWrites(bar: String)

object TestClassWithWrites {

  implicit lazy val writes: OWrites[TestClassWithWrites] =
    Json.writes[TestClassWithWrites]
}
