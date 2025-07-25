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

package repositories

import config.FrontendAppConfig
import itbase.ItSpecBase
import models.{SensitiveFormats, UserAnswers}
import org.mongodb.scala.*
import org.mongodb.scala.bson.{BsonDocument, BsonString}
import org.scalatest.freespec.AnyFreeSpec
import play.api.libs.json.Json
import services.DateTimeService
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec extends ItSpecBase with DefaultPlayMongoRepositorySupport[UserAnswers] {

  private val config: FrontendAppConfig                   = app.injector.instanceOf[FrontendAppConfig]
  private val dateTimeService: DateTimeService            = app.injector.instanceOf[DateTimeService]
  implicit private val sensitiveFormats: SensitiveFormats = app.injector.instanceOf[SensitiveFormats]

  override protected val repository: SessionRepository = new SessionRepository(mongoComponent, config, dateTimeService)

  private val internalId1 = "internalId1"
  private val internalId2 = "internalId2"

  private val userAnswers1 = UserAnswers(internalId1, Json.obj("foo" -> "bar"), dateTimeService.now)
  private val userAnswers2 = UserAnswers(internalId2, Json.obj("bar" -> "foo"), dateTimeService.now)

  "SessionRepository" - {

    "get" - {

      "must return UserAnswers when match found for internal ID" in {

        insert(userAnswers1).futureValue

        val result = repository.get(internalId1).futureValue

        result.value.id mustEqual userAnswers1.id
        result.value.data mustEqual userAnswers1.data

        result.value.lastUpdated `equals` userAnswers1.lastUpdated.truncatedTo(
          java.time.temporal.ChronoUnit.MILLIS
        ) mustEqual true
      }

      "must return None when no match found for internal ID" in {

        val result = repository.get(internalId2).futureValue

        result must not be defined
      }
    }

    "set" - {

      "must update document when it already exists" in {

        val setResult = repository.set(userAnswers1).futureValue

        val getResult = repository.get(internalId1).futureValue.value

        setResult mustEqual true
        getResult.id mustEqual userAnswers1.id
        getResult.data mustEqual userAnswers1.data

        getResult.lastUpdated `isAfter` userAnswers1.lastUpdated.truncatedTo(
          java.time.temporal.ChronoUnit.MILLIS
        ) mustEqual true
      }

      "must create new document when it doesn't already exist" in {

        val setResult = repository.set(userAnswers2).futureValue

        val getResult = repository.get(internalId2).futureValue.value

        setResult mustEqual true
        getResult.id mustEqual userAnswers2.id
        getResult.data mustEqual userAnswers2.data

        getResult.lastUpdated `isAfter` userAnswers2.lastUpdated.truncatedTo(
          java.time.temporal.ChronoUnit.MILLIS
        ) mustEqual true
      }
    }

    "must ensure indexes" in {

      val indexes = repository.collection.listIndexes().toFuture().futureValue

      indexes.length mustEqual 2

      indexes(1).get("name").get mustEqual BsonString("user-answers-last-updated-index")
      indexes(1).get("key").get mustEqual BsonDocument("lastUpdated" -> 1)
      indexes(1).get("expireAfterSeconds").get.asNumber().intValue() mustEqual config.mongoDbTtl
    }
  }

}
