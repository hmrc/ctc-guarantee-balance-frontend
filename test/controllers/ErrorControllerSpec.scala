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
import play.api.test.FakeRequest
import play.api.test.Helpers._

class ErrorControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  "Error Controller" - {

    "Bad Request" - {

      "must return BadRequest" in {

        val request = FakeRequest(GET, routes.ErrorController.badRequest().url)

        val result = route(app, request).value

        status(result) mustEqual BAD_REQUEST
      }
    }

    "Not Found" - {

      "must return notFound" in {

        val request = FakeRequest(GET, routes.ErrorController.notFound().url)

        val result = route(app, request).value

        status(result) mustEqual NOT_FOUND
      }
    }

    "Technical difficulties" - {

      "must return InternalServerError" in {

        val request = FakeRequest(GET, routes.ErrorController.technicalDifficulties().url)

        val result = route(app, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }

    "Internal server error" - {

      "must return InternalServerError" in {

        val request = FakeRequest(GET, routes.ErrorController.internalServerError().url)

        val result = route(app, request).value

        status(result) mustEqual INTERNAL_SERVER_ERROR
      }
    }
  }
}
