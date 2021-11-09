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

import base.SpecBase
import models.UserAnswers
import models.requests.{IdentifierRequest, OptionalDataRequest}
import org.mockito.Mockito._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar with ScalaFutures {

  class Harness(sessionRepository: SessionRepository) extends DataRetrievalActionImpl(sessionRepository) {
    def callTransform[A](request: IdentifierRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  "Data Retrieval Action" - {

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {

        forAll(arbitrary[String], arbitrary[Boolean]) {
          case (id, isEnrolled) =>
            val sessionRepository = mock[SessionRepository]
            when(sessionRepository.get(id)) thenReturn Future(None)
            val action = new Harness(sessionRepository)

            val futureResult = action.callTransform(IdentifierRequest(fakeRequest, id, isEnrolled))

            whenReady(futureResult) {
              result =>
                result.isEnrolled mustBe isEnrolled
                result.internalId mustBe id
                result.userAnswers.isEmpty mustBe true
            }
        }
      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {

        forAll(arbitrary[String], arbitrary[Boolean]) {
          case (id, isEnrolled) =>
            val sessionRepository = mock[SessionRepository]
            when(sessionRepository.get(id)) thenReturn Future(Some(new UserAnswers(id)))
            val action = new Harness(sessionRepository)

            val futureResult = action.callTransform(IdentifierRequest(fakeRequest, id, isEnrolled))

            whenReady(futureResult) {
              result =>
                result.isEnrolled mustBe isEnrolled
                result.internalId mustBe id
                result.userAnswers.isDefined mustBe true
                result.userAnswers.get.id mustBe id
            }
        }
      }
    }
  }
}
