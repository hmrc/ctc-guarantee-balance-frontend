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

package handlers

import base.{AppWithDefaultMockFixtures, SpecBase}
import matchers.JsonMatchers
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify}
import org.scalacheck.Gen
import org.scalatest.OptionValues
import play.api.libs.typedmap.TypedMap
import play.api.mvc.request.{RemoteConnection, RequestTarget}
import play.api.mvc.{Headers, RequestHeader, Result}
import play.api.test.Helpers._

import scala.concurrent.Future

// scalastyle:off magic.number
class ErrorHandlerSpec extends SpecBase with JsonMatchers with AppWithDefaultMockFixtures with OptionValues {

  private lazy val handler: ErrorHandler = app.injector.instanceOf[ErrorHandler]

  "must render NotFound page when given a 404" in {

    val result: Future[Result] = handler.onClientError(new FakeRequestHeader, 404)

    val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

    verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

    status(result) mustBe NOT_FOUND
    templateCaptor.getValue mustEqual "notFound.njk"
  }

  "must render BadRequest page when given a client error (400-499)" in {

    forAll(Gen.choose(400, 499).suchThat(_ != 404)) {
      clientErrorCode =>
        beforeEach()

        val result: Future[Result] = handler.onClientError(new FakeRequestHeader, clientErrorCode)

        val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

        status(result) mustBe clientErrorCode
        templateCaptor.getValue mustEqual "badRequest.njk"
    }
  }

  "must render TechnicalDifficulties page when given any other error" in {

    forAll(Gen.choose(500, 599)) {
      clientErrorCode =>
        beforeEach()

        val result: Future[Result] = handler.onClientError(new FakeRequestHeader, clientErrorCode)

        val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

        status(result) mustBe clientErrorCode
        templateCaptor.getValue mustEqual "technicalDifficulties.njk"
    }
  }

  class FakeRequestHeader extends RequestHeader {
    override val target: RequestTarget = RequestTarget("/context/some-path", "/context/some-path", Map.empty)

    override def method: String = "POST"

    override def version: String = "HTTP/1.1"

    override def headers: Headers = new Headers(Seq.empty)

    override def connection: RemoteConnection = RemoteConnection("", secure = true, None)

    override def attrs: TypedMap = TypedMap()
  }

}
// scalastyle:on magic.number
