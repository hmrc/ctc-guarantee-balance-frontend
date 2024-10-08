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

package viewModels.audit

import base.SpecBase
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json

import java.time.LocalDateTime

class SuccessfulBalanceAuditModelSpec extends SpecBase with MockitoSugar {

  "SuccessfulBalanceAuditModel" - {

    "must apply the correct details when apply is called" in {

      val localDateTime = LocalDateTime.now
      val actualDetails = SuccessfulBalanceAuditModel
        .apply(
          "123456789800",
          "1222",
          "internalId",
          localDateTime,
          200,
          "£1,000,000"
        )
        .detail

      actualDetails mustEqual expectedDetails(localDateTime)

    }

  }

  private def expectedDetails(localDateTime: LocalDateTime) = Json.obj(
    "eoriNumber"               -> "-",
    "guaranteeReferenceNumber" -> "123456789800",
    "accessCode"               -> "1222",
    "internalId"               -> "internalId",
    "transactionDateTime"      -> localDateTime.toString,
    "status"                   -> 200,
    "balance"                  -> "£1,000,000"
  )

}
