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

package models.values

import base.SpecBase
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.{JsError, JsString, Json}

import java.util.UUID

class BalanceIdSpec extends SpecBase {

  private val balanceIdKey: String = "balanceId"

  "balanceIdFormat" - {
    "must serialise" in {
      val uuid           = "22b9899e-24ee-48e6-a189-97d1f45391c4"
      val balanceId      = BalanceId(UUID.fromString(uuid))
      val expectedResult = JsString(uuid)
      val result         = Json.toJson(balanceId)
      result.mustEqual(expectedResult)
    }

    "must deserialise" - {
      "when json in expected shape" in {
        val uuid           = "22b9899e-24ee-48e6-a189-97d1f45391c4"
        val json           = JsString(uuid)
        val result         = json.validate[BalanceId]
        val expectedResult = BalanceId(UUID.fromString(uuid))
        result.get.mustEqual(expectedResult)
      }
    }

    "must fail to deserialise" - {
      "when json in unexpected shape" in {
        val json   = Json.obj("foo" -> "bar")
        val result = json.validate[BalanceId]
        result mustBe a[JsError]
      }
    }
  }

  "pathBinder" - {

    "must bind a UUID" in {
      forAll(arbitrary[UUID]) {
        uuid =>
          BalanceId.pathBinder.bind(balanceIdKey, uuid.toString).value mustEqual BalanceId(uuid)
      }
    }

    "must fail to bind a non-UUID" in {
      forAll(arbitrary[String]) {
        notAUuid =>
          BalanceId.pathBinder.bind(balanceIdKey, notAUuid).isLeft mustEqual true
      }
    }

    "must unbind a UUID" in {
      forAll(arbitrary[UUID]) {
        uuid =>
          BalanceId.pathBinder.unbind(balanceIdKey, BalanceId(uuid)) mustEqual uuid.toString
      }
    }
  }
}
