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

import config.FrontendAppConfig
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.bson.collection.BSONSerializationPack
import reactivemongo.api.indexes.Index.Aux
import reactivemongo.api.indexes.IndexType
import reactivemongo.play.json.collection.JSONCollection

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
private[repositories] class SessionCollection @Inject() (
  mongo: ReactiveMongoApi,
  config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends (() => Future[JSONCollection]) {

  private val collectionName: String = "user-answers"

  private val lastUpdatedIndexName: String = "user-answers-last-updated-index"

  override def apply(): Future[JSONCollection] =
    for {
      collection <- mongo.database.map(_.collection[JSONCollection](collectionName))
      _          <- collection.ensureIndexes
    } yield collection

  implicit class RichJSONCollection(collection: JSONCollection) {

    def rebuildIndexes: Future[Unit] = for {
      _ <- dropIndexes
      _ <- ensureIndexes
    } yield ()

    def dropIndexes: Future[Int] = collection.indexesManager.dropAll()

    def ensureIndexes: Future[Boolean] = {

      lazy val lastUpdatedIndex: Aux[BSONSerializationPack.type] = SimpleMongoIndexConfig(
        key = Seq("lastUpdated" -> IndexType.Ascending),
        name = Some(lastUpdatedIndexName),
        expireAfterSeconds = Some(config.mongoDbTtl)
      )

      collection.indexesManager.ensure(lastUpdatedIndex)
    }
  }

}
