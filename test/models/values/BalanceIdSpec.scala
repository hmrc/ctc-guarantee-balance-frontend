/*
 * Copyright 2022 HM Revenue & Customs
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

import java.util.UUID

class BalanceIdSpec extends SpecBase {

  private val balanceIdKey: String = "balanceId"

  "must bind a UUID" in {
    forAll(arbitrary[UUID]) {
      uuid =>
        BalanceId.pathBinder.bind(balanceIdKey, uuid.toString) mustBe Right(BalanceId(uuid))
    }
  }

  "must fail to bind a non-UUID" in {
    forAll(arbitrary[String]) {
      notAUuid =>
        BalanceId.pathBinder.bind(balanceIdKey, notAUuid) mustBe 'left
    }
  }

  "must unbind a UUID" in {
    forAll(arbitrary[UUID]) {
      uuid =>
        BalanceId.pathBinder.unbind(balanceIdKey, BalanceId(uuid)) mustBe uuid.toString
    }
  }
}
