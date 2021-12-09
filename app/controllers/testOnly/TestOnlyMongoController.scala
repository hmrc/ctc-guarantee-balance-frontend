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

package controllers.testOnly

import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.play.json.collection.JSONCollection
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyMongoController @Inject() (
  override val messagesApi: MessagesApi,
  mongo: ReactiveMongoApi,
  val controllerComponents: MessagesControllerComponents
)(implicit
  ec: ExecutionContext
) extends FrontendBaseController {

  def dropMongoCollections(): Action[AnyContent] = Action.async {
    _ =>
      val collectionNames: Seq[String] = Seq("user-answers", "locks")
      Future.sequence(collectionNames.map(dropMongoCollection)).map {
        x =>
          if (x.forall(identity)) Ok else InternalServerError
      }
  }

  private def dropMongoCollection(name: String): Future[Boolean] = {
    val collection: Future[JSONCollection] = mongo.database.map(_.collection[JSONCollection](name))
    collection.flatMap(_.drop(failIfNotFound = false))
  }
}
