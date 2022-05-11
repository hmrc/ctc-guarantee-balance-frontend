/*
 * Copyright 2022 HM Revenue & Customs
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

import forms.Constants._
import forms.behaviours.{FieldBehaviours, StringFieldBehaviours}
import play.api.data.{Field, FormError}

class EoriNumberFormProviderSpec extends StringFieldBehaviours with FieldBehaviours {

  val requiredKey            = "eoriNumber.error.required"
  val maxLengthKey           = "eoriNumber.error.maxLength"
  val minLengthKey           = "eoriNumber.error.minLength"
  val invalidCharactersKey   = "eoriNumber.error.invalidCharacters"
  val invalidFormatKey       = "eoriNumber.error.invalidFormat"
  val invalidPrefixFormatKey = "eoriNumber.error.prefix"

  val form = new EoriNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form = form,
      fieldName = fieldName,
      validDataGenerator = stringsWithMaxLength(maxEoriNumberLength)
    )

    behave like mandatoryField(
      form = form,
      fieldName = fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form = form,
      fieldName = fieldName,
      regex = alphaNumericRegex,
      gen = stringsOfLength(maxEoriNumberLength),
      invalidKey = invalidCharactersKey
    )

    s"must not bind strings with correct prefix and suffix but over max length" in {

      val expectedError = FormError(fieldName, maxLengthKey, Seq(maxEoriNumberLength))
      val invalidString = "xi1234567890123432"
      val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
      result.errors must contain(expectedError)
    }

    s"must not bind strings with correct prefix and suffix but under min length" in {

      val expectedError = FormError(fieldName, minLengthKey, Seq(minEoriNumberLength))
      val invalidString = "xi123456789"
      val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
      result.errors must contain(expectedError)
    }

    s"must not bind strings with correct prefix but invalid suffix" in {

      val expectedError = FormError(fieldName, invalidFormatKey, Seq(eoriNumberRegex))
      val invalidString = "xixi1234567890"
      val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
      result.errors must contain(expectedError)
    }

    s"must not bind strings with wrong Prefix" in {

      val expectedError = FormError(fieldName, invalidPrefixFormatKey, Seq(eoriNumberPrefixRegex))
      val invalidString = "AB123456789876"
      val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
      result.errors must contain(expectedError)
    }

    "must remove spaces on bound strings" in {
      val result = form.bind(Map(fieldName -> " GB 123 456 789 123"))
      result.errors mustEqual Nil
      result.get mustEqual "GB123456789123"
    }

  }
}
