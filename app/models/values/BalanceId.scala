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

package models.values

import play.api.libs.json.{Format, Json}
import play.api.mvc.PathBindable

import java.util.UUID

case class BalanceId(value: UUID) extends AnyVal

object BalanceId {

  implicit val balanceIdFormat: Format[BalanceId] =
    Json.valueFormat[BalanceId]

  implicit def pathBinder(implicit uuidBinder: PathBindable[UUID]): PathBindable[BalanceId] = new PathBindable[BalanceId] {

    override def bind(key: String, value: String): Either[String, BalanceId] =
      for {
        uuid <- uuidBinder.bind(key, value)
      } yield BalanceId(uuid)

    override def unbind(key: String, value: BalanceId): String =
      uuidBinder.unbind(key, value.value)
  }
}
