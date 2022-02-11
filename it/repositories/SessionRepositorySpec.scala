/*
 * Copyright 2022 HM Revenue & Customs
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

import config.FrontendAppConfig
import models.UserAnswers
import org.mongodb.scala.bson.{BsonDocument, BsonInt64, BsonString}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec extends AnyFreeSpec with Matchers with DefaultPlayMongoRepositorySupport[UserAnswers] with GuiceOneAppPerSuite with OptionValues {

  private val config: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

  override protected def repository = new SessionRepository(mongoComponent, config)

  private val internalId1 = "internalId1"
  private val internalId2 = "internalId2"

  private val userAnswers1 = UserAnswers(internalId1, Json.obj("foo" -> "bar"))
  private val userAnswers2 = UserAnswers(internalId2, Json.obj("bar" -> "foo"))

  "SessionRepository" - {

    "get" - {

      "must return UserAnswers when match found for internal ID" in {

        insert(userAnswers1).futureValue

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

    "must ensure indexes" in {

      val indexes = mongoDatabase.getCollection("user-answers").listIndexes().toFuture().futureValue

      indexes.length mustEqual 2

      indexes(1).get("name").get mustEqual BsonString("user-answers-last-updated-index")
      indexes(1).get("key").get mustEqual BsonDocument("lastUpdated" -> 1)
      indexes(1).get("expireAfterSeconds").get mustEqual BsonInt64(config.mongoDbTtl)

    }
  }

}
