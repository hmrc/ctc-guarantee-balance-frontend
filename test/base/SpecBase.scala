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

package base

import generators.Generators
import models.UserAnswers
import org.scalatest._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{Json, Writes}
import queries.Settable

import scala.util.{Success, Try}

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with OptionValues
    with TryValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with Generators {

  val configKey = "config"

  val userAnswersId = "id"

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId, Json.obj())

  implicit class RichUserAnswers(userAnswers: UserAnswers) {

    def setOption[A](page: Settable[A], optionalValue: Option[A])(implicit writes: Writes[A]): Try[UserAnswers] =
      optionalValue match {
        case Some(value) => userAnswers.set[A](page, value)
        case None        => Success(userAnswers)
      }
  }

}
