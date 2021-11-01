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

package connectors

import java.time.Instant
import java.util.UUID

import base.{AppWithDefaultMockFixtures, SpecBase}
import com.github.tomakehurst.wiremock.client.WireMock._
import helper.WireMockServerHandler
import models.backend.{BalanceRequestPending, BalanceRequestPendingExpired, BalanceRequestSuccess}
import models.requests.BalanceRequest
import models.values._
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.{ContentTypes, HeaderNames, Status}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

class GuaranteeBalanceConnectorSpec extends SpecBase with WireMockServerHandler with ScalaCheckPropertyChecks with AppWithDefaultMockFixtures {

  override lazy val app: Application = applicationBuilder()
    .configure(
      conf = "microservice.services.common-transit-convention-guarantee-balance.port" -> server.port()
    )
    .build()

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  private val submitBalanceRequestUrl                      = s"/balances"
  private def queryBalanceRequestUrlFor(balanceId: String) = s"/balances/$balanceId"

  private lazy val connector = app.injector.instanceOf[GuaranteeBalanceConnector]

  private val request = BalanceRequest(
    TaxIdentifier("taxid"),
    GuaranteeReference("guarref"),
    AccessCode("1234")
  )

  private val requestAsJsonString: String = Json.stringify(Json.toJson(request))

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

        val expectedResponse = BalanceRequestSuccess(BigDecimal(3.14), CurrencyCode("EUR"))

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

      "must return the HttpResponse when there is an unexpected response" in {
        val errorResponses = Gen.chooseNum(401, 599).suchThat(_ != Status.NOT_FOUND)

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

            val response = result.left.get

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
            |   "balanceId": "22b9899e-24ee-48e6-a189-97d1f45391c4",
            |   "enrolmentId": "testEnrolmentId",
            |   "taxIdentifier": "taxid",
            |   "guaranteeReference": "guarref",
            |   "requestedAt": "$requestedAt",
            |   "completedAt": "$completedAt",
            |   "response": {
            |     "balance": 3.14,
            |     "currency": "EUR"
            |   }
            | }
            |""".stripMargin

        server.stubFor(
          get(urlEqualTo(queryBalanceRequestUrlFor("22b9899e-24ee-48e6-a189-97d1f45391c4")))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(balanceRequestSuccessResponseJson))
        )

        val expectedResponse = BalanceRequestSuccess(BigDecimal(3.14), CurrencyCode("EUR"))

        val result = connector.queryPendingBalance(BalanceId(testUuid)).futureValue
        result mustBe Right(expectedResponse)
      }

      "must return balance pending response for Ok with no returned response" in {
        val requestedAt = Instant.now().minusSeconds(300)
        val completedAt = Instant.now().minusSeconds(1)
        val balanceRequestSuccessResponseJson: String =
          s"""
             | {
             |   "balanceId": "22b9899e-24ee-48e6-a189-97d1f45391c4",
             |   "enrolmentId": "testEnrolmentId",
             |   "taxIdentifier": "taxid",
             |   "guaranteeReference": "guarref",
             |   "requestedAt": "$requestedAt",
             |   "completedAt": "$completedAt"
             | }
             |""".stripMargin

        server.stubFor(
          get(urlEqualTo(queryBalanceRequestUrlFor("22b9899e-24ee-48e6-a189-97d1f45391c4")))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(okJson(balanceRequestSuccessResponseJson))
        )

        val balanceId = BalanceId(testUuid)

        val result = connector.queryPendingBalance(balanceId).futureValue
        result mustBe Right(BalanceRequestPending(balanceId))
      }

      "must return the HttpResponse when there is an unexpected response" in {
        val errorResponses = Gen.chooseNum(401, 599).suchThat(_ != Status.NOT_FOUND)

        forAll(errorResponses) {
          errorResponse =>
            server.stubFor(
              get(urlEqualTo(queryBalanceRequestUrlFor("22b9899e-24ee-48e6-a189-97d1f45391c4")))
                .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
                .willReturn(aResponse().withStatus(errorResponse))
            )

            val result = connector.queryPendingBalance(BalanceId(testUuid)).futureValue

            val response = result.left.get

            response.status mustBe errorResponse
        }
      }

      "must return pending expired when a NotFound is returned" in {
        val balanceId = BalanceId(testUuid)

        server.stubFor(
          get(urlEqualTo(queryBalanceRequestUrlFor("22b9899e-24ee-48e6-a189-97d1f45391c4")))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .willReturn(aResponse().withStatus(Status.NOT_FOUND))
        )

        val result = connector.queryPendingBalance(balanceId).futureValue

        result mustBe Right(BalanceRequestPendingExpired(balanceId))
      }
    }
  }
}
