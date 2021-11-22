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
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.mongo.lock.{Lock, MongoLockRepository}
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport
import play.api.inject.bind
import uk.gov.hmrc.mongo.TimestampSupport

import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec
    extends AnyFreeSpec
    with Matchers
    with MongoSuite
    with ScalaFutures
    with BeforeAndAfterEach
    with IntegrationPatience
    with GuiceOneAppPerSuite
    with OptionValues
    with DefaultPlayMongoRepositorySupport[Lock] {

  private lazy val timestampSupport: TimestampSupport = mock[TimestampSupport]

  override lazy val repository = new MongoLockRepository(mongoComponent, timestampSupport)

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(
      bind[MongoLockRepository].toInstance(repository)
    )
    .build()

  private lazy val sessionRepository = app.injector.instanceOf[SessionRepository]

  private val internalId1 = "internalId1"
  private val internalId2 = "internalId2"

  private val userAnswers1 = UserAnswers(internalId1, Json.obj("foo" -> "bar"))
  private val userAnswers2 = UserAnswers(internalId2, Json.obj("bar" -> "foo"))

  override def beforeEach(): Unit = {
    super.beforeEach()
    database.flatMap {
      _.collection[JSONCollection]("user-answers")
        .insert(ordered = false)
        .many(Seq(userAnswers1, userAnswers2))
    }.futureValue
  }

  override def afterEach(): Unit = {
    super.afterEach()
    database.flatMap(_.drop())
  }

  "SessionRepository" - {

    "get" - {

      "must return UserAnswers when given an internal ID" in {

        val result = sessionRepository.get(internalId1).futureValue

        result.value.id mustBe userAnswers1.id
        result.value.data mustBe userAnswers1.data
      }

      "must return None when no UserAnswers match internal ID" in {

        val result = sessionRepository.get("foo").futureValue

        result mustBe None
      }
    }

    "set" - {

      "must create new document when given valid UserAnswers" in {

        val setResult = sessionRepository.set(userAnswers1).futureValue

        val getResult = sessionRepository.get(internalId1).futureValue.value

        setResult mustBe true
        getResult.id mustBe userAnswers1.id
        getResult.data mustBe userAnswers1.data
      }
    }
  }

}
