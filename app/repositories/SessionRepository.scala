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
import models.{SensitiveFormats, UserAnswers}
import org.mongodb.scala.model._
import services.DateTimeService
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import org.mongodb.scala._

@Singleton
class SessionRepository @Inject() (
  mongoComponent: MongoComponent,
  config: FrontendAppConfig,
  dateTimeService: DateTimeService
)(implicit ec: ExecutionContext, sensitiveFormats: SensitiveFormats)
    extends PlayMongoRepository[UserAnswers](
      mongoComponent = mongoComponent,
      collectionName = "user-answers",
      domainFormat = UserAnswers.format,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions().name("user-answers-last-updated-index").expireAfter(config.mongoDbTtl.toLong, TimeUnit.SECONDS)
        )
      ),
      replaceIndexes = config.replaceIndexes
    ) {

  def get(id: String): Future[Option[UserAnswers]] =
    collection
      .findOneAndUpdate(Filters.eq("_id", id), Updates.set("lastUpdated", dateTimeService.now), FindOneAndUpdateOptions().upsert(false))
      .toFutureOption()

  def set(userAnswers: UserAnswers): Future[Boolean] = {

    val updatedUserAnswers = userAnswers.copy(lastUpdated = dateTimeService.now)

    collection
      .replaceOne(Filters.eq("_id", userAnswers.id), updatedUserAnswers, ReplaceOptions().upsert(true))
      .toFuture()
      .map(_.wasAcknowledged())
  }
}
