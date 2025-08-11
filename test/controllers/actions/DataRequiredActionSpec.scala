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

package controllers.actions

import base.SpecBase
import controllers.routes
import models.UserAnswers
import models.requests.{DataRequest, OptionalDataRequest}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.Helpers.*

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRequiredActionSpec extends SpecBase {

  object Harness extends DataRequiredActionImpl {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "Data Required Action" - {

    "when there are no UserAnswers" - {

      "must return Left and redirect to session expired" in {

        val harness = Harness.callRefine(OptionalDataRequest(fakeRequest, "id", None))

        val result = harness.map(_.left.value)

        status(result) mustEqual 303
        redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
      }
    }

    "when there are UserAnswers" - {

      "must return Right with DataRequest" in {

        val dateTime = Instant.now()

        val userAnswers = UserAnswers("eoriNumber", Json.obj(), dateTime)

        val result = Harness.callRefine(OptionalDataRequest(fakeRequest, "id", Some(userAnswers)))

        whenReady(result) {
          result =>
            result.value.userAnswers mustEqual userAnswers
            result.value.internalId mustEqual "id"
        }
      }
    }
  }
}
