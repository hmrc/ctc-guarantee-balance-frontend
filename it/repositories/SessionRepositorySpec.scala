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

import itBase.ItSpecBase
import models.UserAnswers
import org.scalatest.OptionValues
import play.api.libs.json.Json
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec extends ItSpecBase with MongoSuite with OptionValues {

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

        val repository = app.injector.instanceOf[SessionRepository]

        val result = repository.get(internalId1).futureValue

        result.value.id mustBe userAnswers1.id
        result.value.data mustBe userAnswers1.data
      }

      "must return None when no UserAnswers match internal ID" in {

        val repository = app.injector.instanceOf[SessionRepository]

        val result = repository.get("foo").futureValue

        result mustBe None
      }
    }

    "set" - {

      "must create new document when given valid UserAnswers" in {

        val repository = app.injector.instanceOf[SessionRepository]

        val setResult = repository.set(userAnswers1).futureValue

        val getResult = repository.get(internalId1).futureValue.value

        setResult mustBe true
        getResult.id mustBe userAnswers1.id
        getResult.data mustBe userAnswers1.data
      }
    }
  }

}
