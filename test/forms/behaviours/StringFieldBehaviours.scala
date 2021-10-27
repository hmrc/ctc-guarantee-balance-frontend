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

package forms.behaviours

import org.scalacheck.Gen
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.{Field, Form, FormError}
import wolfendale.scalacheck.regexp.RegexpGen

trait StringFieldBehaviours extends FieldBehaviours {

  def fieldWithMaxLength(form: Form[_], fieldName: String, maxLength: Int, lengthError: FormError): Unit =
    s"must not bind strings longer than $maxLength characters" in {

      forAll(stringsLongerThan(maxLength) -> "longString") {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors shouldEqual Seq(lengthError)
      }
    }

  def fieldWithInvalidCharacters(form: Form[_], fieldName: String, invalidKey: String, length: Int, args: Any*) =
    s"must not bind strings with invalid characters" in {

      val expectedError          = Seq(FormError(fieldName, invalidKey, args.toList))
      val generator: Gen[String] = RegexpGen.from(s"[!£^*(){}_+=:;|`~<>,±üçñèé@]{$length}")

      forAll(generator) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must equal(expectedError)
      }
    }

}
