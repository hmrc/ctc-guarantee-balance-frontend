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
import org.mongodb.scala.model.{IndexModel, IndexOptions, Indexes}
import uk.gov.hmrc.mongo.lock
import uk.gov.hmrc.mongo.{MongoComponent, MongoUtils, TimestampSupport}

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MongoLockRepository @Inject() (
  mongoComponent: MongoComponent,
  timestampSupport: TimestampSupport,
  config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends lock.MongoLockRepository(mongoComponent, timestampSupport) {

  lazy val expiryTimeIndex: IndexModel = IndexModel(
    Indexes.ascending("expiryTime"),
    IndexOptions().name("locks-expiry-time-index").expireAfter(config.rateLimitDuration, TimeUnit.SECONDS)
  )

  override def ensureIndexes: Future[Seq[String]] =
    MongoUtils.ensureIndexes(collection, indexes :+ expiryTimeIndex, replaceIndexes = false)
}
