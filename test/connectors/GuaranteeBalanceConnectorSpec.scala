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

package connectors

import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptyList
import com.github.tomakehurst.wiremock.client.WireMock._
import helper.WireMockServerHandler
import models.backend._
import models.backend.errors.FunctionalError
import models.requests._
import models.values.ErrorType.{InvalidDataErrorType, NotMatchedErrorType}
import models.values._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.{ContentTypes, HeaderNames, Status}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import java.util.UUID

class GuaranteeBalanceConnectorSpec extends SpecBase with WireMockServerHandler with ScalaCheckPropertyChecks with AppWithDefaultMockFixtures {

  override lazy val app: Application = applicationBuilder()
    .configure(
      conf = "microservice.services.common-transit-convention-guarantee-balance.port" -> server.port()
    )
    .build()

  implicit private val hc: HeaderCarrier = HeaderCarrier()
  private val grn: GuaranteeReference    = GuaranteeReference("guarref")

  private val submitBalanceRequestUrl                         = s"/balances"
  private def queryBalanceRequestUrlFor(balanceId: BalanceId) = s"/balances/${balanceId.value}"
  private def submitBalanceRequestV2Url(grn: String)          = s"/$grn/balance"

  private lazy val connector = app.injector.instanceOf[GuaranteeBalanceConnector]

  private val request = BalanceRequest(
    TaxIdentifier("taxid"),
    GuaranteeReference("guarref"),
    AccessCode("1234")
  )

  private val requestV2 = BalanceRequestV2(
    AccessCode("1234")
  )

  private val requestAsJsonString: String   = Json.stringify(Json.toJson(request))
  private val requestV2AsJsonString: String = Json.stringify(Json.toJson(requestV2))

  private val testUuid = UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4")

  "GuaranteeBalanceConnector" - {

    "submitBalanceRequest" - {

      "must return balance success response for Ok" in {
        val balanceRequestSuccessResponseJson: String =
          """
            | {
            |   "response": {
            |     "balance": 3.14,
            |     "currency": "EUR"
            |   }
            | }
            |""".stripMargin

        server.stubFor(
          post(urlEqualTo(submitBalanceRequestUrl))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .withRequestBody(equalToJson(requestAsJsonString))
            .willReturn(okJson(balanceRequestSuccessResponseJson))
        )

        val expectedResponse = BalanceRequestSuccess(BigDecimal(3.14), Some(CurrencyCode("EUR")))

        val result = connector.submitBalanceRequest(request).futureValue
        result mustBe Right(expectedResponse)
      }

      "must return balance pending for Accepted" in {
        val expectedUuid = testUuid
        val balanceRequestPendingResponseJson: String =
          s"""
             | {
             |   "balanceId": "22b9899e-24ee-48e6-a189-97d1f45391c4"
             | }
             |""".stripMargin

        server.stubFor(
          post(urlEqualTo(submitBalanceRequestUrl))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .withRequestBody(equalToJson(requestAsJsonString))
            .willReturn(
              aResponse()
                .withStatus(Status.ACCEPTED)
                .withHeader(HeaderNames.CONTENT_TYPE, ContentTypes.JSON)
                .withBody(balanceRequestPendingResponseJson)
            )
        )

        val expectedResponse = BalanceRequestPending(BalanceId(expectedUuid))

        val result = connector.submitBalanceRequest(request).futureValue
        result mustBe Right(expectedResponse)
      }

      "must return balance request not matched for a functional error with error type 12" in {
        val balanceRequestNotMatchedJson: String =
          """
            | {
            |   "code": "FUNCTIONAL_ERROR",
            |   "message": "The request was rejected by the guarantee management system",
            |   "response": {
            |     "errors": [
            |       {
            |         "errorType": 12,
            |         "errorPointer": "Foo.Bar(1).Baz"
            |       }
            |     ]
            |   }
            | }
            |""".stripMargin

        server.stubFor(
          post(urlEqualTo(submitBalanceRequestUrl))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .withRequestBody(equalToJson(requestAsJsonString))
            .willReturn(
              aResponse()
                .withStatus(Status.BAD_REQUEST)
                .withHeader(HeaderNames.CONTENT_TYPE, ContentTypes.JSON)
                .withBody(balanceRequestNotMatchedJson)
            )
        )

        val result = connector.submitBalanceRequest(request).futureValue
        result mustBe Right(BalanceRequestNotMatched("Foo.Bar(1).Baz"))
      }

      "must return unsupported guarantee balance type for a functional error with error type 14 and Pointer GRR(1).GQY(1).Query identifier" in {
        val balanceRequestNotMatchedJson: String =
          """
            | {
            |   "code": "FUNCTIONAL_ERROR",
            |   "message": "The request was rejected by the guarantee management system",
            |   "response": {
            |     "errors": [
            |       {
            |         "errorType": 14,
            |         "errorPointer": "GRR(1).GQY(1).Query identifier",
            |         "errorReason": "R261"
            |       }
            |     ]
            |   }
            | }
            |""".stripMargin

        server.stubFor(
          post(urlEqualTo(submitBalanceRequestUrl))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .withRequestBody(equalToJson(requestAsJsonString))
            .willReturn(
              aResponse()
                .withStatus(Status.BAD_REQUEST)
                .withHeader(HeaderNames.CONTENT_TYPE, ContentTypes.JSON)
                .withBody(balanceRequestNotMatchedJson)
            )
        )

        val result = connector.submitBalanceRequest(request).futureValue
        result mustBe Right(BalanceRequestUnsupportedGuaranteeType)
      }

      "must return rate limit balance type when we have an http response TOO_MANY_REQUESTS" in {
        val balanceRequestNotMatchedJson: String =
          """
            | {
            |   "code": "FUNCTIONAL_ERROR",
            |   "message": "The request was rejected by the guarantee management system",
            |   "response": {
            |     "errors": [
            |       {
            |         "errorType": 14,
            |         "errorPointer": "GRR(1).GQY(1).Query identifier",
            |         "errorReason": "R261"
            |       }
            |     ]
            |   }
            | }
            |""".stripMargin

        server.stubFor(
          post(urlEqualTo(submitBalanceRequestUrl))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .withRequestBody(equalToJson(requestAsJsonString))
            .willReturn(
              aResponse()
                .withStatus(Status.TOO_MANY_REQUESTS)
                .withHeader(HeaderNames.CONTENT_TYPE, ContentTypes.JSON)
                .withBody(balanceRequestNotMatchedJson)
            )
        )

        val result = connector.submitBalanceRequest(request).futureValue
        result mustBe Right(BalanceRequestRateLimit)
      }

      "must return an Http error when we get a response with error type 14 and other Pointer" in {
        val functionErrorJson: String =
          """
            | {
            |   "code": "FUNCTIONAL_ERROR",
            |   "message": "The request was rejected by the guarantee management system",
            |   "response": {
            |     "errors": [
            |       {
            |         "errorType": 14,
            |         "errorPointer": "Foo.Bar(1).Baz"
            |       }
            |     ]
            |   }
            | }
            |""".stripMargin

        server.stubFor(
          post(urlEqualTo(submitBalanceRequestUrl))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .withRequestBody(equalToJson(requestAsJsonString))
            .willReturn(
              aResponse()
                .withStatus(Status.BAD_REQUEST)
                .withHeader(HeaderNames.CONTENT_TYPE, ContentTypes.JSON)
                .withBody(functionErrorJson)
            )
        )

        val result   = connector.submitBalanceRequest(request).futureValue
        val response = result.left.value
        response.status mustBe Status.BAD_REQUEST
      }

      "must return the HttpResponse for a functional error with inconsequential error type" in {
        val knownErrorTypes = Seq(NotMatchedErrorType)

        forAll(
          arbitrary[Int].suchThat(
            x => !knownErrorTypes.contains(ErrorType(x))
          )
        ) {
          errorType =>
            val json: String =
              s"""
                 | {
                 |   "code": "FUNCTIONAL_ERROR",
                 |   "message": "The request was rejected by the guarantee management system",
                 |   "response": {
                 |     "errors": [
                 |       {
                 |         "errorType": $errorType,
                 |         "errorPointer": "Foo.Bar(1).Baz"
                 |       }
                 |     ]
                 |   }
                 | }
                 |""".stripMargin

            server.stubFor(
              post(urlEqualTo(submitBalanceRequestUrl))
                .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
                .withRequestBody(equalToJson(requestAsJsonString))
                .willReturn(
                  aResponse()
                    .withStatus(Status.BAD_REQUEST)
                    .withHeader(HeaderNames.CONTENT_TYPE, ContentTypes.JSON)
                    .withBody(json)
                )
            )

            val result = connector.submitBalanceRequest(request).futureValue

            val response = result.left.value

            response.status mustBe Status.BAD_REQUEST
        }
      }

      "must return the HttpResponse when there is an unexpected response" in {
        val errorResponses = Gen.chooseNum(400, 599).suchThat(_ != Status.TOO_MANY_REQUESTS)

        forAll(errorResponses) {
          errorResponse =>
            server.stubFor(
              post(urlEqualTo(submitBalanceRequestUrl))
                .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
                .withRequestBody(equalToJson(requestAsJsonString))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )

            val result = connector.submitBalanceRequest(request).futureValue

            val response = result.left.value

            response.status mustBe errorResponse
        }
      }
    }

    "queryPendingBalance" - {
      "must return balance success response for Ok with a returned response" in {
        val requestedAt = Instant.now().minusSeconds(300)
        val completedAt = Instant.now().minusSeconds(1)
        val balanceRequestSuccessResponseJson: String =
          s"""
             | {
             |   "request" : {
             |     "balanceId": "22b9899e-24ee-48e6-a189-97d1f45391c4",
             |     "taxIdentifier": "taxid",
             |     "guaranteeReference": "guarref",
             |     "requestedAt": "$requestedAt",
             |     "completedAt": "$completedAt",
             |     "response": {
             |       "balance": 3.14,
             |       "currency": "EUR"
             |     }
             |   }
             | }
             |""".stripMargin

        val balanceId = BalanceId(testUuid)

        server.stubFor(
          get(urlEqualTo(queryBalanceRequestUrlFor(balanceId)))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(balanceRequestSuccessResponseJson))
        )

        val expectedResponse = BalanceRequestSuccess(BigDecimal(3.14), Some(CurrencyCode("EUR")))

        val result = connector.queryPendingBalance(BalanceId(testUuid)).futureValue
        result mustBe Right(expectedResponse)
      }

      "must return balance pending response for Ok with no returned response" in {
        val balanceId   = BalanceId(testUuid)
        val requestedAt = Instant.now().minusSeconds(59)
        val completedAt = Instant.now().minusSeconds(1)
        val balanceRequestSuccessResponseJson: String =
          s"""
             | {
             |  "request": {
             |    "balanceId": "22b9899e-24ee-48e6-a189-97d1f45391c4",
             |    "taxIdentifier": "taxid",
             |    "guaranteeReference": "guarref",
             |    "requestedAt": "$requestedAt",
             |    "completedAt": "$completedAt"
             |  }
             | }
             |""".stripMargin

        server.stubFor(
          get(urlEqualTo(queryBalanceRequestUrlFor(balanceId)))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(balanceRequestSuccessResponseJson))
        )

        val result = connector.queryPendingBalance(balanceId).futureValue
        result mustBe Right(BalanceRequestPending(balanceId))
      }

      "must return balance pending expired if we get a pending response that's too old" in {
        val balanceId   = BalanceId(testUuid)
        val requestedAt = Instant.now().minusSeconds(61)
        val completedAt = Instant.now().minusSeconds(1)
        val balanceRequestSuccessResponseJson: String =
          s"""
             | {
             |  "request": {
             |    "balanceId": "22b9899e-24ee-48e6-a189-97d1f45391c4",
             |    "taxIdentifier": "taxid",
             |    "guaranteeReference": "guarref",
             |    "requestedAt": "$requestedAt",
             |    "completedAt": "$completedAt"
             |  }
             | }
             |""".stripMargin

        server.stubFor(
          get(urlEqualTo(queryBalanceRequestUrlFor(balanceId)))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(balanceRequestSuccessResponseJson))
        )

        val result = connector.queryPendingBalance(balanceId).futureValue
        result mustBe Right(BalanceRequestPendingExpired(balanceId))
      }

      "must return the HttpResponse when there is an unexpected response" in {
        val errorResponses = Gen.chooseNum(401, 599).suchThat(_ != Status.NOT_FOUND)
        val balanceId      = BalanceId(testUuid)

        forAll(errorResponses) {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(queryBalanceRequestUrlFor(balanceId)))
                .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
                .willReturn(aResponse().withStatus(errorResponse))
            )

            val result = connector.queryPendingBalance(BalanceId(testUuid)).futureValue

            val response = result.left.value

            response.status mustBe errorResponse
        }
      }

      "must return pending expired when a NotFound is returned" in {
        val balanceId = BalanceId(testUuid)

        server.stubFor(
          get(urlEqualTo(queryBalanceRequestUrlFor(balanceId)))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(aResponse().withStatus(Status.NOT_FOUND))
        )

        val result = connector.queryPendingBalance(balanceId).futureValue

        result mustBe Right(BalanceRequestPendingExpired(balanceId))
      }

      "must return balance request not matched for a functional error with error type 12" in {
        val balanceId   = BalanceId(testUuid)
        val requestedAt = Instant.now().minusSeconds(30)
        val completedAt = Instant.now().minusSeconds(1)
        val balanceRequestNotMatchedJson: String =
          s"""
             | {
             |   "request" : {
             |     "balanceId": "22b9899e-24ee-48e6-a189-97d1f45391c4",
             |     "taxIdentifier": "taxid",
             |     "guaranteeReference": "guarref",
             |     "requestedAt": "$requestedAt",
             |     "completedAt": "$completedAt",
             |     "response":{"errors":[{"errorType":12,"errorPointer":"Foo.Bar(1).Baz"}],"status":"FUNCTIONAL_ERROR"}
             |   }
             | }
             |""".stripMargin

        server.stubFor(
          get(urlEqualTo(queryBalanceRequestUrlFor(balanceId)))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(balanceRequestNotMatchedJson))
        )

        val result = connector.queryPendingBalance(balanceId).futureValue
        result mustBe Right(BalanceRequestNotMatched("Foo.Bar(1).Baz"))
      }

      "must return unsupported guaranteebalance type for a functional error with error type 14" in {
        val balanceId   = BalanceId(testUuid)
        val requestedAt = Instant.now().minusSeconds(30)
        val completedAt = Instant.now().minusSeconds(1)
        val balanceRequestNotMatchedJson: String =
          s"""
             | {
             |   "request" : {
             |     "balanceId": "22b9899e-24ee-48e6-a189-97d1f45391c4",
             |     "taxIdentifier": "taxid",
             |     "guaranteeReference": "guarref",
             |     "requestedAt": "$requestedAt",
             |     "completedAt": "$completedAt",
             |     "response":{
             |        "errors":[{"errorType":14,"errorPointer":"GRR(1).GQY(1).Query identifier", "errorReason": "R261"}],
             |        "status":"FUNCTIONAL_ERROR"
             |     }
             |   }
             | }
             |""".stripMargin

        server.stubFor(
          get(urlEqualTo(queryBalanceRequestUrlFor(balanceId)))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(balanceRequestNotMatchedJson))
        )

        val result = connector.queryPendingBalance(balanceId).futureValue
        result mustBe Right(BalanceRequestUnsupportedGuaranteeType)
      }

      "must return an functional error response with error type 14 and other Pointer" in {
        val balanceId   = BalanceId(testUuid)
        val requestedAt = Instant.now().minusSeconds(30)
        val completedAt = Instant.now().minusSeconds(1)
        val functionErrorJson: String =
          s"""
             | {
             |   "request" : {
             |     "balanceId": "22b9899e-24ee-48e6-a189-97d1f45391c4",
             |     "taxIdentifier": "taxid",
             |     "guaranteeReference": "guarref",
             |     "requestedAt": "$requestedAt",
             |     "completedAt": "$completedAt",
             |     "response":{"errors":[{"errorType":14,"errorPointer":"GRR(1).GQY(1).Query identifier"}],"status":"FUNCTIONAL_ERROR"}
             |   }
             | }
             |""".stripMargin

        server.stubFor(
          get(urlEqualTo(queryBalanceRequestUrlFor(balanceId)))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(functionErrorJson))
        )

        val result          = connector.queryPendingBalance(balanceId).futureValue
        val functionalError = FunctionalError(InvalidDataErrorType, "GRR(1).GQY(1).Query identifier", None)
        result mustBe Right(BalanceRequestFunctionalError(NonEmptyList[FunctionalError](functionalError, Nil)))
      }
    }

    "submitBalanceRequestV2" - {

      "must return balance success response for Ok" in {
        val balanceRequestSuccessResponseJson: String =
          """
            | {
            |   "balance": 3.14
            | }
            |""".stripMargin

        server.stubFor(
          post(urlEqualTo(submitBalanceRequestV2Url(grn.value)))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.2.0+json"))
            .withRequestBody(equalToJson(requestV2AsJsonString))
            .willReturn(okJson(balanceRequestSuccessResponseJson))
        )

        val expectedResponse = BalanceRequestSuccess(BigDecimal(3.14), None)

        val result = connector.submitBalanceRequestV2(requestV2, grn.value).futureValue
        result mustBe Right(expectedResponse)
      }

      "must return rate limit balance type when we have an http response TOO_MANY_REQUESTS" in {
        val tooManyRequestsJson: String =
          """
            | {
            |   "code": "TOO_MANY_REQUESTS",
            |   "message": "The request was rejected by the guarantee management system"
            | }
            |""".stripMargin

        server.stubFor(
          post(urlEqualTo(submitBalanceRequestV2Url(grn.value)))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.2.0+json"))
            .withRequestBody(equalToJson(requestV2AsJsonString))
            .willReturn(
              aResponse()
                .withStatus(Status.TOO_MANY_REQUESTS)
                .withHeader(HeaderNames.CONTENT_TYPE, ContentTypes.JSON)
                .withBody(tooManyRequestsJson)
            )
        )

        val result = connector.submitBalanceRequestV2(requestV2, grn.value).futureValue
        result mustBe Right(BalanceRequestRateLimit)
      }

      "must return non matched when we have an http response NOT_FOUND" in {
        val notFoundJson: String =
          """
            | {
            |   "code": "NOT_FOUND",
            |   "message": "Not found"
            | }
            |""".stripMargin

        server.stubFor(
          post(urlEqualTo(submitBalanceRequestV2Url(grn.value)))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.2.0+json"))
            .withRequestBody(equalToJson(requestV2AsJsonString))
            .willReturn(
              aResponse()
                .withStatus(Status.NOT_FOUND)
                .withHeader(HeaderNames.CONTENT_TYPE, ContentTypes.JSON)
                .withBody(notFoundJson)
            )
        )

        val result = connector.submitBalanceRequestV2(requestV2, grn.value).futureValue
        result mustBe Right(BalanceRequestNotMatched(notFoundJson))
      }

      "must return non matched when we have an http response BAD_REQUEST with invalid GRN" in {
        val notFoundJson: String =
          """
            | {
            |   "code": "BAD_REQUEST",
            |   "message": "The guarantee reference number is not in the correct format"
            | }
            |""".stripMargin

        server.stubFor(
          post(urlEqualTo(submitBalanceRequestV2Url(grn.value)))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.2.0+json"))
            .withRequestBody(equalToJson(requestV2AsJsonString))
            .willReturn(
              aResponse()
                .withStatus(Status.NOT_FOUND)
                .withHeader(HeaderNames.CONTENT_TYPE, ContentTypes.JSON)
                .withBody(notFoundJson)
            )
        )

        val result = connector.submitBalanceRequestV2(requestV2, grn.value).futureValue
        result mustBe Right(BalanceRequestNotMatched(notFoundJson))
      }

      "must return the HttpResponse when there is a BAD_REQUEST" in {
        val errorResponses = Gen
          .chooseNum(400, 499)
          .suchThat(_ != Status.TOO_MANY_REQUESTS)
          .suchThat(_ != Status.NOT_FOUND)

        forAll(errorResponses) {
          errorResponse =>
            server.stubFor(
              post(urlEqualTo(submitBalanceRequestV2Url(grn.value)))
                .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.2.0+json"))
                .withRequestBody(equalToJson(requestV2AsJsonString))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )

            val result = connector.submitBalanceRequestV2(requestV2, grn.value).futureValue

            val response = result.left.value

            response.status mustBe errorResponse
        }
      }

      "must return the HttpResponse when there is an INTERNAL_SERVER_ERROR" in {
        val errorResponses = Gen.chooseNum(500, 599).suchThat(_ != Status.TOO_MANY_REQUESTS)

        forAll(errorResponses) {
          errorResponse =>
            server.stubFor(
              post(urlEqualTo(submitBalanceRequestV2Url(grn.value)))
                .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.2.0+json"))
                .withRequestBody(equalToJson(requestV2AsJsonString))
                .willReturn(
                  aResponse()
                    .withStatus(errorResponse)
                )
            )

            val result = connector.submitBalanceRequestV2(requestV2, grn.value).futureValue

            val response = result.left.value

            response.status mustBe errorResponse
        }
      }
    }
  }
}
