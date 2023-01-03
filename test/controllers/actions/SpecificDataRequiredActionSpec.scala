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
import models.UserAnswers
import models.requests._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import play.api.http.Status.SEE_OTHER
import play.api.libs.json.{JsPath, Reads}
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.Helpers.{defaultAwaitTimeout, redirectLocation, status}
import queries.Gettable

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpecificDataRequiredActionSpec extends SpecBase with ScalaCheckPropertyChecks {

  private val internalId = "internalId"

  private class Harness1[T1](page: Gettable[T1])(implicit rds: Reads[T1]) extends SpecificDataRequiredAction1[T1](page) {

    def callRefine[A](
      request: DataRequest[A]
    ): Future[Either[Result, SpecificDataRequestProvider1[T1]#SpecificDataRequest[A]]] =
      refine(request)
  }

  private class Harness2[T1, T2](page: Gettable[T2])(implicit rds: Reads[T2]) extends SpecificDataRequiredAction2[T1, T2](page) {

    def callRefine[A](
      request: SpecificDataRequestProvider1[T1]#SpecificDataRequest[A]
    ): Future[Either[Result, SpecificDataRequestProvider2[T1, T2]#SpecificDataRequest[A]]] =
      refine(request)
  }

  private case object FakePage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ "foo"
  }

  "Specific Data Required Action" - {

    "getFirst" - {

      def request(userAnswers: UserAnswers): DataRequest[AnyContentAsEmpty.type] =
        DataRequest(fakeRequest, internalId, userAnswers)

      "when required data not present in user answers" - {
        "must redirect to session expired" in {

          val action = new Harness1(FakePage)

          val futureResult = action.callRefine(request(emptyUserAnswers))

          whenReady(futureResult) {
            r =>
              val result = Future.successful(r.left.value)
              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
          }
        }
      }

      "when required data present in user answers" - {
        "must add value to request" in {

          val action = new Harness1(FakePage)

          forAll(arbitrary[String]) {
            str =>
              val userAnswers = emptyUserAnswers.setValue(FakePage, str)

              val futureResult = action.callRefine(request(userAnswers))

              whenReady(futureResult) {
                _.value.arg mustBe str
              }
          }
        }
      }
    }

    "getSecond" - {

      def request(userAnswers: UserAnswers, arg1: String): SpecificDataRequestProvider1[String]#SpecificDataRequest[AnyContentAsEmpty.type] =
        new SpecificDataRequestProvider1[String].SpecificDataRequest(fakeRequest, internalId, userAnswers, arg1)

      "when required data not present in user answers" - {
        "must redirect to session expired" in {

          val action = new Harness2[String, String](FakePage)

          forAll(arbitrary[String]) {
            str1 =>
              val futureResult = action.callRefine(request(emptyUserAnswers, str1))

              whenReady(futureResult) {
                r =>
                  val result = Future.successful(r.left.value)
                  status(result) mustEqual SEE_OTHER
                  redirectLocation(result).value mustEqual controllers.routes.SessionExpiredController.onPageLoad().url
              }
          }
        }
      }

      "when required data present in user answers" - {
        "must add value to request" in {

          val action = new Harness2[String, String](FakePage)

          forAll(arbitrary[String], arbitrary[String]) {
            (str1, str2) =>
              val userAnswers = emptyUserAnswers.setValue(FakePage, str2)

              val futureResult = action.callRefine(request(userAnswers, str1))

              whenReady(futureResult) {
                r =>
                  r.value.arg._1 mustBe str1
                  r.value.arg._2 mustBe str2
              }
          }
        }
      }
    }
  }
}
