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
import models.values.{BalanceId, ErrorType, GuaranteeReference, TaxIdentifier}
import play.api.libs.json.{JsSuccess, Json}

import java.time.Instant
import java.util.UUID

class PostResponseSpec extends SpecBase {

  "PostBalanceRequestSuccessResponse" - {

    "serialize and deserialize to/from JSON" in {
      val balanceRequestSuccess = BalanceRequestSuccess(BigDecimal(3.14), None)
      val successResponse       = PostBalanceRequestSuccessResponse(balanceRequestSuccess)

      val json = Json.toJson(successResponse)
      json.validate[PostBalanceRequestSuccessResponse] mustBe JsSuccess(successResponse)
    }
  }

  "PostBalanceRequestPendingResponse" - {

    "serialize and deserialize to/from JSON" in {
      val balanceId       = BalanceId(UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4"))
      val pendingResponse = PostBalanceRequestPendingResponse(balanceId)

      val json = Json.toJson(pendingResponse)
      json.validate[PostBalanceRequestPendingResponse] mustBe JsSuccess(pendingResponse)
    }
  }

  "PostBalanceRequestFunctionalErrorResponse" - {

    "serialize and deserialize to/from JSON" in {
      val errorResponse = BalanceRequestFunctionalError(
        errors = NonEmptyList.fromList(List(FunctionalError(ErrorType(1), "str", None))).get
      )
      val functionalErrorResponse = PostBalanceRequestFunctionalErrorResponse(
        code = "ERR123",
        message = "Some error message",
        response = errorResponse
      )

      val json = Json.toJson(functionalErrorResponse)
      json.validate[PostBalanceRequestFunctionalErrorResponse] mustBe JsSuccess(functionalErrorResponse)
    }
  }

  "GetBalanceRequestResponse" - {

    "deserialize from a JSON string" in {
      val balanceId = "22b9899e-24ee-48e6-a189-97d1f45391c4"
      val jsonString =
        s"""
          {
            "request": {
              "balanceId": "$balanceId",
              "taxIdentifier": "tax-id-123",
              "guaranteeReference": "guarantee-ref-123",
              "requestedAt": "2024-09-18T10:15:30Z"
            }
          }
        """

      val expectedRequest = PendingBalanceRequest(
        balanceId = BalanceId(UUID.fromString(balanceId)),
        taxIdentifier = TaxIdentifier("tax-id-123"),
        guaranteeReference = GuaranteeReference("guarantee-ref-123"),
        requestedAt = Instant.parse("2024-09-18T10:15:30.00Z"),
        completedAt = None,
        response = None
      )

      val expectedResponse = GetBalanceRequestResponse(expectedRequest)

      val json = Json.parse(jsonString)
      json.validate[GetBalanceRequestResponse] mustBe JsSuccess(expectedResponse)
    }
  }
}
