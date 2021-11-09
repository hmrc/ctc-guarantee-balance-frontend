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

package controllers.actions

import base.{AppWithDefaultMockFixtures, SpecBase}
import controllers.routes
import models.UserAnswers
import models.requests.{DataRequest, OptionalDataRequest}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.EitherValues
import play.api.mvc.Result
import play.api.test.Helpers._

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRequiredActionSpec extends SpecBase with EitherValues with AppWithDefaultMockFixtures {

  object Harness extends DataRequiredActionImpl {
    def callRefine[A](request: OptionalDataRequest[A]): Future[Either[Result, DataRequest[A]]] = refine(request)
  }

  "Data Required Action" - {

    "when there are no UserAnswers" - {

      "must return Left and redirect to session expired" in {

        forAll(arbitrary[String], arbitrary[Boolean]) {
          case (id, isEnrolled) =>
            val harness = Harness.callRefine(OptionalDataRequest(fakeRequest, id, None, isEnrolled))

            val result = harness.map(_.left.value)

            status(result) mustBe 303
            redirectLocation(result).value mustBe routes.SessionExpiredController.onPageLoad().url
        }
      }
    }

    "when there are UserAnswers" - {

      "must return Right with DataRequest" in {

        val dateTime = LocalDateTime.now()

        forAll(arbitrary[String], arbitrary[Boolean]) {
          case (id, isEnrolled) =>
            val result = Harness.callRefine(OptionalDataRequest(fakeRequest, id, Some(UserAnswers(id, lastUpdated = dateTime)), isEnrolled))

            whenReady(result) {
              result =>
                result.value.userAnswers mustBe UserAnswers(id, lastUpdated = dateTime)
                result.value.internalId mustBe id
                result.value.isEnrolled mustBe isEnrolled
            }
        }
      }
    }
  }
}
