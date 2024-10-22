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

package models.backend

import base.SpecBase
import cats.data.NonEmptyList
import models.backend.errors.FunctionalError
import models.values.*
import play.api.libs.json.Json

import java.time.Instant
import java.util.UUID

class PostResponseSpec extends SpecBase {

  "PostBalanceRequestSuccessResponse" - {

    "deserialize from JSON" in {
      val json = Json.parse("""
          |{
          |  "response" : {
          |    "balance" : 3.14,
          |    "currency" : "EUR"
          |  }
          |}
          |""".stripMargin)

      val expectedResult = {
        val balanceRequestSuccess = BalanceRequestSuccess(BigDecimal(3.14), Some(CurrencyCode("EUR")))
        PostBalanceRequestSuccessResponse(balanceRequestSuccess)
      }

      val result = json.validate[PostBalanceRequestSuccessResponse]

      result.get.mustBe(expectedResult)
    }
  }

  "PostBalanceRequestPendingResponse" - {

    "deserialize from JSON" in {
      val uuid = "22b9899e-24ee-48e6-a189-97d1f45391c4"

      val json = Json.parse(s"""
          |{
          |  "balanceId" : "$uuid"
          |}
          |""".stripMargin)

      val expectedResult = {
        val balanceId = BalanceId(UUID.fromString(uuid))
        PostBalanceRequestPendingResponse(balanceId)
      }

      val result = json.validate[PostBalanceRequestPendingResponse]

      result.get.mustBe(expectedResult)
    }
  }

  "PostBalanceRequestFunctionalErrorResponse" - {

    "deserialize from JSON" in {
      val json = Json.parse("""
          |{
          |  "code" : "ERR123",
          |  "message" : "Some error message",
          |  "response" : {
          |    "errors" : [
          |      {
          |        "errorType" : 14,
          |        "errorPointer" : "GRR(1).GQY(1).Query identifier",
          |        "errorReason" : "R261"
          |      }
          |    ]
          |  }
          |}
          |""".stripMargin)

      val expectedResult = PostBalanceRequestFunctionalErrorResponse(
        code = "ERR123",
        message = "Some error message",
        response = BalanceRequestFunctionalError(
          errors = NonEmptyList.one(
            FunctionalError(
              errorType = ErrorType(14: Int),
              errorPointer = "GRR(1).GQY(1).Query identifier",
              errorReason = Some("R261")
            )
          )
        )
      )

      val result = json.validate[PostBalanceRequestFunctionalErrorResponse]

      result.get.mustBe(expectedResult)
    }
  }

  "GetBalanceRequestResponse" - {

    "deserialize from a JSON string" in {
      val balanceId = "22b9899e-24ee-48e6-a189-97d1f45391c4"

      val json = Json.parse(s"""
           |{
           |  "request": {
           |    "balanceId": "$balanceId",
           |    "taxIdentifier": "tax-id-123",
           |    "guaranteeReference": "guarantee-ref-123",
           |    "requestedAt": "2024-09-18T10:15:30Z"
           |  }
           |}
           |""".stripMargin)

      val expectedResult = GetBalanceRequestResponse(
        PendingBalanceRequest(
          balanceId = BalanceId(UUID.fromString(balanceId)),
          taxIdentifier = TaxIdentifier("tax-id-123"),
          guaranteeReference = GuaranteeReference("guarantee-ref-123"),
          requestedAt = Instant.parse("2024-09-18T10:15:30.00Z"),
          completedAt = None,
          response = None
        )
      )

      val result = json.validate[GetBalanceRequestResponse]

      result.get.mustBe(expectedResult)
    }
  }
}
