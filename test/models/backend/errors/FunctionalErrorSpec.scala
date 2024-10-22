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

package models.backend.errors

import base.SpecBase
import models.values.ErrorType
import play.api.libs.json.Json

class FunctionalErrorSpec extends SpecBase {

  "deserialize from JSON" in {
    val json = Json.parse("""
        |{
        |  "errorType" : 14,
        |  "errorPointer" : "GRR(1).GQY(1).Query identifier",
        |  "errorReason" : "R261"
        |}
        |""".stripMargin)

    val expectedResult = FunctionalError(
      errorType = ErrorType(14: Int),
      errorPointer = "GRR(1).GQY(1).Query identifier",
      errorReason = Some("R261")
    )

    val result = json.validate[FunctionalError]

    result.get.mustBe(expectedResult)
  }
}
