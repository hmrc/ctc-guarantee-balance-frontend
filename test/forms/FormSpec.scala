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

package forms

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.{Form, FormError}

trait FormSpec extends AnyFreeSpec with OptionValues with Matchers {

  def checkForError(form: Form[?], data: Map[String, String], expectedErrors: Seq[FormError]) =
    form
      .bind(data)
      .fold(
        formWithErrors => {
          for (error <- expectedErrors) formWithErrors.errors must contain(FormError(error.key, error.message, error.args))
          formWithErrors.errors.size mustEqual expectedErrors.size
        },
        _ => fail("Expected a validation error when binding the form, but it was bound successfully.")
      )

  def error(key: String, value: String, args: Any*) = Seq(FormError(key, value, args))

  lazy val emptyForm = Map[String, String]()
}
