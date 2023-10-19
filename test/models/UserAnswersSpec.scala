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

package models

import base.{AppWithDefaultMockFixtures, SpecBase}
import org.scalacheck.Arbitrary.arbitrary
import pages.QuestionPage
import play.api.libs.json._
import play.api.test.Helpers.running

import java.time.Instant

class UserAnswersSpec extends SpecBase with AppWithDefaultMockFixtures {

  "UserAnswers" - {

    case object TestPage extends QuestionPage[String] {
      override def path: JsPath = JsPath \ "foo"
    }

    "get" - {

      "must return data when defined" in {

        val userAnswers = UserAnswers("eoriNumber", JsObject(Map("foo" -> JsString("bar"))), Instant.now())

        userAnswers.get(TestPage) mustBe Some("bar")
      }

      "must return None when not defined" in {

        val userAnswers = UserAnswers("eoriNumber", Json.obj(), Instant.now())

        userAnswers.get(TestPage) mustBe None
      }
    }

    "set" - {

      "must set data" in {

        val userAnswers = UserAnswers("eoriNumber", Json.obj(), Instant.now())

        val expectedUserAnswers = userAnswers.copy(data = JsObject(Map("foo" -> JsString("bar"))))

        val result = userAnswers.set(TestPage, "bar").toOption.value.data

        result mustBe expectedUserAnswers.data
      }
    }

    "remove" - {

      "must remove data" in {

        val userAnswers = UserAnswers("eoriNumber", JsObject(Map("foo" -> JsString("bar"))), Instant.now())

        val expectedUserAnswers = userAnswers.copy(data = Json.obj())

        val result = userAnswers.remove(TestPage).toOption.value.data

        result mustBe expectedUserAnswers.data
      }
    }

    "clear" - {

      "must clear everything" in {

        forAll(arbitrary[UserAnswers]) {
          _.clear.data mustEqual Json.obj()
        }
      }
    }

    "formats" - {

      val userAnswers = UserAnswers(
        id = userAnswersId,
        data = Json.obj(),
        lastUpdated = Instant.ofEpochMilli(1662546803472L)
      )

      "when encryption enabled" - {
        val app = applicationBuilder()
          .configure("encryption.enabled" -> true)
          .build()

        running(app) {
          val sensitiveFormats                     = app.injector.instanceOf[SensitiveFormats]
          implicit val format: Format[UserAnswers] = UserAnswers.format(sensitiveFormats)

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$userAnswersId",
               |  "data" : "T+FWrvLPJMKyRZ1aoW8rdZmETyL89CdpWxaog0joG6B/hxCF",
               |  "lastUpdated" : {
               |    "$$date" : {
               |      "$$numberLong" : "1662546803472"
               |    }
               |  }
               |}
               |""".stripMargin)

          "read correctly" in {
            val result = json.as[UserAnswers]
            result mustBe userAnswers
          }

          "write and read correctly" in {
            val result = Json.toJson(userAnswers).as[UserAnswers]
            result mustBe userAnswers
          }
        }
      }

      "when encryption disabled" - {
        val app = applicationBuilder()
          .configure("encryption.enabled" -> false)
          .build()

        running(app) {
          val sensitiveFormats                     = app.injector.instanceOf[SensitiveFormats]
          implicit val format: Format[UserAnswers] = UserAnswers.format(sensitiveFormats)

          val json: JsValue = Json.parse(s"""
               |{
               |  "_id" : "$userAnswersId",
               |  "data" : {},
               |  "lastUpdated" : {
               |    "$$date" : {
               |      "$$numberLong" : "1662546803472"
               |    }
               |  }
               |}
               |""".stripMargin)

          "must read correctly" in {
            val result = json.as[UserAnswers]
            result mustBe userAnswers
          }

          "write correctly" in {
            val result = Json.toJson(userAnswers)
            result mustBe json
          }
        }
      }
    }
  }
}
