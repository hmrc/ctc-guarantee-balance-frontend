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

  private val userAnswer1 = UserAnswers(internalId1, Json.obj("foo" -> "bar"))
  private val userAnswer2 = UserAnswers(internalId2, Json.obj("bar" -> "foo"))

  override def beforeEach(): Unit = {
    super.beforeEach()
    database.flatMap {
      _.collection[JSONCollection]("user-answers")
        .insert(ordered = false)
        .many(Seq(userAnswer1, userAnswer2))
    }.futureValue
  }

  override def afterEach(): Unit = {
    super.afterEach()
    database.flatMap(_.drop())
  }

  "SessionRepository" - {

    "get" - {

      "must return UserAnswers when given an internal ID" in {

        val result = repository.get(internalId1).futureValue

        result.value.id mustBe userAnswer1.id
        result.value.data mustBe userAnswer1.data
      }

      "must return None when no UserAnswers match internal ID" in {

        val result = repository.get("foo").futureValue

        result mustBe None
      }
    }

    "set" - {

      "must create new document when given valid UserAnswers" in {

        val userAnswer = UserAnswers(internalId1, Json.obj("foo" -> "bar"))

        val setResult = repository.set(userAnswer).futureValue

        val getResult = repository.get(internalId1).futureValue.value

        setResult mustBe true
        getResult.id mustBe userAnswer.id
        getResult.data mustBe userAnswer.data
      }
    }
  }

}
