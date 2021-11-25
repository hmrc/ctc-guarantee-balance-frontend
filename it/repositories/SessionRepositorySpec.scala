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

package repositories

import models.UserAnswers
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with MongoSuite
    with ScalaFutures
    with BeforeAndAfterEach
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with OptionValues {

  private val repository = app.injector.instanceOf[SessionRepository]

  private val internalId1 = "internalId1"
  private val internalId2 = "internalId2"

  private val userAnswers1 = UserAnswers(internalId1, Json.obj("foo" -> "bar"))
  private val userAnswers2 = UserAnswers(internalId2, Json.obj("bar" -> "foo"))

  override def beforeEach(): Unit = {
    super.beforeEach()
    database.flatMap {
      _.collection[JSONCollection]("user-answers")
        .insert(ordered = false)
        .one(userAnswers1)
    }.futureValue
  }

  override def afterEach(): Unit = {
    super.afterEach()
    database.flatMap(_.drop()).futureValue
  }

  "SessionRepository" - {

    "get" - {

      "must return UserAnswers when match found for internal ID" in {

        val result = repository.get(internalId1).futureValue

        result.value.id mustBe userAnswers1.id
        result.value.data mustBe userAnswers1.data
        result.value.lastUpdated isEqual userAnswers1.lastUpdated mustBe true
      }

      "must return None when no match found for internal ID" in {

        val result = repository.get(internalId2).futureValue

        result mustBe None
      }
    }

    "set" - {

      "must update document when it already exists" in {

        val setResult = repository.set(userAnswers1).futureValue

        val getResult = repository.get(internalId1).futureValue.value

        setResult mustBe true
        getResult.id mustBe userAnswers1.id
        getResult.data mustBe userAnswers1.data
        getResult.lastUpdated isAfter userAnswers1.lastUpdated mustBe true
      }

      "must create new document when it doesn't already exist" in {

        val setResult = repository.set(userAnswers2).futureValue

        val getResult = repository.get(internalId2).futureValue.value

        setResult mustBe true
        getResult.id mustBe userAnswers2.id
        getResult.data mustBe userAnswers2.data
        getResult.lastUpdated isAfter userAnswers2.lastUpdated mustBe true
      }
    }

    "must remove document after TTL has elapsed" in {

      val testTtl: Int = 0
      val delay: Int   = testTtl + 3

      val app = new GuiceApplicationBuilder()
        .configure("mongodb.timeToLiveInSeconds" -> testTtl)
        .build()

      val repository = app.injector.instanceOf[SessionRepository]

      val setResult = repository.set(userAnswers2).futureValue
      setResult mustBe true

      Thread.sleep(delay * 1000)

      val getResult = repository.get(internalId2).futureValue

      getResult mustNot be(defined)
    }
  }

}
