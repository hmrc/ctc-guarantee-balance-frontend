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

package models

import play.api.libs.json._
import queries._
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import java.time.Instant
import scala.util.{Failure, Success, Try}

final case class UserAnswers(
  id: String,
  data: JsObject,
  lastUpdated: Instant
) {

  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def set[A](page: Settable[A] & Gettable[A], newValue: A)(implicit writes: Writes[A], rds: Reads[A]): Try[UserAnswers] = {

    lazy val hasValueChanged = !get(page).contains(newValue)

    val updatedData = data.setObject(page.path, Json.toJson(newValue)) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(errors) =>
        Failure(JsResultException(errors))
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(Some(newValue), updatedAnswers, hasValueChanged)
    }
  }

  def remove[A](page: Settable[A]): Try[UserAnswers] = {

    val updatedData = data.removeObject(page.path) match {
      case JsSuccess(jsValue, _) =>
        Success(jsValue)
      case JsError(_) =>
        Success(data)
    }

    updatedData.flatMap {
      d =>
        val updatedAnswers = copy(data = d)
        page.cleanup(None, updatedAnswers, hasChanged = true)
    }
  }

  def clear: UserAnswers =
    this.copy(data = Json.obj())
}

object UserAnswers {

  import play.api.libs.functional.syntax._

  implicit def reads(implicit sensitiveFormats: SensitiveFormats): Reads[UserAnswers] =
    (
      (__ \ "_id").read[String] and
        (__ \ "data").read[JsObject](sensitiveFormats.jsObjectReads) and
        (__ \ "lastUpdated").read(MongoJavatimeFormats.instantReads)
    )(UserAnswers.apply)

  implicit def writes(implicit sensitiveFormats: SensitiveFormats): OWrites[UserAnswers] =
    (
      (__ \ "_id").write[String] and
        (__ \ "data").write[JsObject](sensitiveFormats.jsObjectWrites) and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantWrites)
    )(
      o => Tuple.fromProductTyped(o)
    )

  implicit def format(implicit sensitiveFormats: SensitiveFormats): Format[UserAnswers] =
    Format(reads, writes)
}
