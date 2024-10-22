/*
 * Copyright 2024 HM Revenue & Customs
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

package models.values

import base.SpecBase
import play.api.libs.json.{JsError, JsString, Json}

class EnrolmentIdSpec extends SpecBase {

  "enrolmentIdFormat" - {
    "must serialise" in {
      val value          = "22b9899e-24ee-48e6-a189-97d1f45391c4"
      val enrolmentId    = EnrolmentId(value)
      val expectedResult = JsString(value)
      val result         = Json.toJson(enrolmentId)
      result.mustBe(expectedResult)
    }

    "must deserialise" - {
      "when json in expected shape" in {
        val value          = "22b9899e-24ee-48e6-a189-97d1f45391c4"
        val json           = JsString(value)
        val result         = json.validate[EnrolmentId]
        val expectedResult = EnrolmentId(value)
        result.get.mustBe(expectedResult)
      }
    }

    "must fail to deserialise" - {
      "when json in unexpected shape" in {
        val json   = Json.obj("foo" -> "bar")
        val result = json.validate[EnrolmentId]
        result.mustBe(a[JsError])
      }
    }
  }
}
