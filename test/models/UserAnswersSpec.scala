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

import base.SpecBase
import org.scalacheck.Arbitrary.arbitrary
import pages.QuestionPage
import play.api.libs.json.{JsObject, JsPath, JsString, Json}

class UserAnswersSpec extends SpecBase {

  "UserAnswers" - {

    case object TestPage extends QuestionPage[String] {
      override def path: JsPath = JsPath \ "foo"
    }

    "get" - {

      "must return data when defined" in {

        val userAnswers = UserAnswers("eoriNumber", JsObject(Map("foo" -> JsString("bar"))))

        userAnswers.get(TestPage) mustBe Some("bar")
      }

      "must return None when not defined" in {

        val userAnswers = UserAnswers("eoriNumber")

        userAnswers.get(TestPage) mustBe None
      }
    }

    "set" - {

      "must set data" in {

        val userAnswers = UserAnswers("eoriNumber")

        val expectedUserAnswers = UserAnswers("eoriNumber", JsObject(Map("foo" -> JsString("bar"))))

        val result = userAnswers.set(TestPage, "bar").toOption.value.data

        result mustBe expectedUserAnswers.data
      }
    }

    "remove" - {

      "must remove data" in {

        val userAnswers = UserAnswers("eoriNumber", JsObject(Map("foo" -> JsString("bar"))))

        val expectedUserAnswers = UserAnswers("eoriNumber")

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
  }
}
