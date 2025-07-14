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

package controllers.testOnly

import itbase.ItSpecBase
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.libs.ws.DefaultBodyWritables.writeableOf_String
import play.api.libs.ws.WSClient
import repositories.SessionRepository

class TestOnlyInputControllerSpec extends ItSpecBase {

  override def guiceApplicationBuilder(): GuiceApplicationBuilder =
    super
      .guiceApplicationBuilder()
      .configure("play.http.router" -> "testOnlyDoNotUseInAppConf.Routes")

  private val wsClient                             = app.injector.instanceOf[WSClient]
  private val sessionRepository: SessionRepository = app.injector.instanceOf[SessionRepository]

  private val baseUrl = s"http://localhost:$port"

  "GET" - {
    "should respond with 200 status" in {
      val response =
        wsClient
          .url(s"$baseUrl/check-transit-guarantee-balance/test-only/input")
          .get()
          .futureValue

      response.status mustEqual 200
    }
  }

  "POST" - {
    "should save input to Mongo" - {
      "when Play framework strips data out of input" in {
        val headers = Seq(
          "Content-Type" -> "application/x-www-form-urlencoded"
        )

        wsClient
          .url(s"$baseUrl/check-transit-guarantee-balance/test-only/input")
          .withHttpHeaders(headers*)
          .post("value=GB000142;<script>print()</script>")
          .futureValue

        val data = sessionRepository.get("test-only").futureValue

        data.get.data mustEqual Json.parse("""
            |{
            |  "testOnlyInput" : "GB000142"
            |}
            |""".stripMargin)
      }
    }
  }
}
