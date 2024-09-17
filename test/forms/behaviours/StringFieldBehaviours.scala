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

package forms.behaviours

import org.scalacheck.Gen
import play.api.data.{Field, Form, FormError}

trait StringFieldBehaviours extends FieldBehaviours {

  def fieldWithMaxLength(form: Form[?], fieldName: String, maxLength: Int, lengthError: FormError): Unit =
    s"must not bind strings longer than $maxLength characters" in {

      forAll(stringsLongerThan(maxLength)) {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors mustEqual Seq(lengthError)
      }
    }

  def fieldWithMinLength(form: Form[?], fieldName: String, minLength: Int, lengthError: FormError): Unit =
    s"must not bind strings shorter than $minLength characters" in {

      forAll(stringsWithMaxLength(minLength - 1)) {
        string =>
          val result = form.bind(Map(fieldName -> string)).apply(fieldName)
          result.errors mustEqual Seq(lengthError)
      }
    }

  def fieldThatDoesNotBindInvalidData(form: Form[?], fieldName: String, regex: String, gen: Gen[String], invalidKey: String): Unit =
    s"must not bind strings which don't match $regex" in {

      val expectedError = FormError(fieldName, invalidKey, Seq(regex))

      forAll(gen.retryUntil(!_.matches(regex))) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }

}
