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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import helper.WireMockServerHandler
import models.backend.BalanceRequestSuccess
import models.requests.BalanceRequest
import models.values.{AccessCode, CurrencyCode, GuaranteeReference, TaxIdentifier}
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.Application
import play.api.http.HeaderNames
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

class GuaranteeBalanceConnectorSpec extends SpecBase with WireMockServerHandler with ScalaCheckPropertyChecks {

  override lazy val app: Application = new GuiceApplicationBuilder()
    .configure(
      conf = "microservice.services.common-transit-convention-guarantee-balance.port" -> server.port()
    )
    .build()

  implicit private val hc: HeaderCarrier = HeaderCarrier()

  private val submitBalanceRequestUrl = s"/balances"

  private lazy val connector = app.injector.instanceOf[GuaranteeBalanceConnector]

  private val balanceRequestSuccessResponseJson: String =
    """
      | {
      |   "response": {
      |     "balance": 3.14,
      |     "currency": "cc"
      |   }
      | }
      |""".stripMargin

  "GuaranteeBalanceConnector" - {

    "submitBalanceRequest" - {

      "must return balance request wrapped in a Some for an Ok" in {
        val request = BalanceRequest(
          TaxIdentifier("taxid"),
          GuaranteeReference("guarref"),
          AccessCode("1234")
        )

        server.stubFor(
          post(urlEqualTo(submitBalanceRequestUrl))
            .withHeader(HeaderNames.ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
            .withRequestBody(equalToJson(Json.stringify(Json.toJson(request))))
            .willReturn(okJson(balanceRequestSuccessResponseJson))
        )

        val expectedResponse = BalanceRequestSuccess(BigDecimal(3.14), CurrencyCode("cc"))

        val result = connector.submitBalanceRequest(request).futureValue
        result mustBe expectedResponse
      }

//      "must return a None for a Not_Found Status" in {
//        server.stubFor(
//          get(urlEqualTo(customsOfficeUrl))
//            .willReturn(
//              aResponse()
//                .withStatus(NOT_FOUND)
//            )
//        )
//
//        val result = connector.getCustomsOffice(customsOfficeId)
//
//        result.futureValue must not be defined
//
//      }
//
//      "must return a None when there is an unexpected response" in {
//        val errorResponses = Gen.chooseNum(400, 599).suchThat(_ != NOT_FOUND)
//
//        forAll(errorResponses) {
//          errorResponse =>
//            server.stubFor(
//              get(urlEqualTo(customsOfficeUrl))
//                .willReturn(
//                  aResponse()
//                    .withStatus(errorResponse)
//                )
//            )
//
//            val result = connector.getCustomsOffice(customsOfficeId)
//
//            result.futureValue must not be defined
//        }
//      }
    }
  }

}
