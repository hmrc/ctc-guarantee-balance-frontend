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

package handlers

import base.SpecBase
import org.scalacheck.Gen
import org.scalatest.OptionValues
import play.api.mvc.Result
import play.api.test.Helpers.*

import scala.concurrent.Future

// scalastyle:off magic.number
class ErrorHandlerSpec extends SpecBase with OptionValues {

  private lazy val handler: ErrorHandler = new ErrorHandler()

  "must redirect to NotFound page when given a 404" in {

    val result: Future[Result] = handler.onClientError(fakeRequest, 404)

    status(result) mustEqual SEE_OTHER
    redirectLocation(result).value mustEqual controllers.routes.ErrorController.notFound().url
  }

  "must redirect to BadRequest page when given a client error (400-499)" in {

    forAll(Gen.choose(400, 499).suchThat(_ != 404)) {
      clientErrorCode =>
        val result: Future[Result] = handler.onClientError(fakeRequest, clientErrorCode)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.badRequest().url
    }
  }

  "must redirect to TechnicalDifficulties page when given any other error" in {

    forAll(Gen.choose(500, 599)) {
      serverErrorCode =>
        val result: Future[Result] = handler.onClientError(fakeRequest, serverErrorCode)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url
    }
  }
}
// scalastyle:on magic.number
