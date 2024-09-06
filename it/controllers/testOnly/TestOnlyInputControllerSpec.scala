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

import models.UserAnswers
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.ws.WSClient
import repositories.SessionRepository
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

class TestOnlyInputControllerSpec
    extends AnyWordSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with GuiceOneServerPerSuite
    with DefaultPlayMongoRepositorySupport[UserAnswers] {

  override protected val repository: PlayMongoRepository[UserAnswers] = app.injector.instanceOf[SessionRepository]

  private val wsClient = app.injector.instanceOf[WSClient]
  private val baseUrl  = s"http://localhost:$port"

  override def fakeApplication(): Application =
    GuiceApplicationBuilder()
      .overrides(
        bind[MongoComponent].toInstance(mongoComponent)
      )
      .configure(
        "metrics.enabled"  -> false,
        "play.http.router" -> "testOnlyDoNotUseInAppConf.Routes"
      )
      .build()

  "GET" should {
    "respond with 200 status" in {
      val response =
        wsClient
          .url(s"$baseUrl/check-transit-guarantee-balance/test-only/input")
          .get()
          .futureValue

      response.status shouldBe 200
    }
  }

//  "POST" should {
//    "save input to Mongo" when {
//      "Play framework strips data out of input" in {
//        val headers = Seq(
//          "Content-Type" -> "application/x-www-form-urlencoded"
//        )
//
//        wsClient
//          .url(s"$baseUrl/check-transit-guarantee-balance/test-only/input")
//          .withHttpHeaders(headers *)
//          .post("value=GB000142;<script>print()</script>")
//          .futureValue
//
//        val documents = findAll().futureValue
//        documents.size shouldBe 1
//        documents.head.data shouldBe Json.parse("""
//            |{
//            |  "testOnlyInput" : "GB000142"
//            |}
//            |""".stripMargin)
//      }
//    }
//  }
}
