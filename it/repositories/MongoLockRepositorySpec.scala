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
import org.mongodb.scala.bson.{BsonDocument, BsonInt64, BsonString}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import uk.gov.hmrc.mongo.TimestampSupport
import uk.gov.hmrc.mongo.lock.Lock
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import scala.concurrent.ExecutionContext.Implicits.global

class MongoLockRepositorySpec extends AnyFreeSpec with Matchers with DefaultPlayMongoRepositorySupport[Lock] with GuiceOneAppPerSuite with OptionValues {

  private val config: FrontendAppConfig          = app.injector.instanceOf[FrontendAppConfig]
  private val timestampSupport: TimestampSupport = app.injector.instanceOf[TimestampSupport]

  override protected def repository = new MongoLockRepository(mongoComponent, timestampSupport, config)

  "MongoLockRepository" - {

    "must ensure indexes" in {

      val indexes = mongoDatabase.getCollection("locks").listIndexes().toFuture().futureValue

      indexes.length mustEqual 2

      indexes(1).get("name").get mustEqual BsonString("locks-expiry-time-index")
      indexes(1).get("key").get mustEqual BsonDocument("expiryTime" -> 1)
      indexes(1).get("expireAfterSeconds").get mustEqual BsonInt64(config.rateLimitDuration)

    }
  }

}
