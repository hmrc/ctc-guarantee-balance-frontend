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

package viewModels.audit

import base.{AppWithDefaultMockFixtures, SpecBase}
import java.time.LocalDateTime

import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json

class SuccessfulBalanceAuditModelSpec extends SpecBase with MockitoSugar {

  "SuccessfulBalanceAuditModel" - {

    "must build the correct details when build is called" in {

      val actualDetails = SuccessfulBalanceAuditModel
        .build(
          LocalDateTime.of(1345, 12, 20, 10, 34, 45),
          "GB1234567890",
          "123456789800",
          "1222",
          200,
          "£1,000,000"
        )
        .detail

      actualDetails mustEqual expectedDetails

    }

  }

  private val expectedDetails = Json.obj(
    "generatedAt"                         -> "1345-12-20T10:34:45",
    "all user info we have from GG login" -> "TBD",
    "Eori Number"                         -> "GB1234567890",
    "Guarantee Reference Number"          -> "123456789800",
    "Access Code"                         -> "1222",
    "status"                              -> 200,
    "Balance"                             -> "£1,000,000"
  )

}
