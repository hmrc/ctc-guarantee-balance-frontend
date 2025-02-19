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

import forms.Constants._
import forms.behaviours.{FieldBehaviours, StringFieldBehaviours}
import org.scalacheck.Gen
import play.api.data.{Field, FormError}

class EoriNumberFormProviderSpec extends StringFieldBehaviours with FieldBehaviours {

  private val requiredKey            = "eoriNumber.error.required"
  private val maxLengthKey           = "eoriNumber.error.maxLength"
  private val minLengthKey           = "eoriNumber.error.minLength"
  private val invalidCharactersKey   = "eoriNumber.error.invalidCharacters"
  private val invalidFormatKey       = "eoriNumber.error.invalidFormat"
  private val invalidPrefixFormatKey = "eoriNumber.error.prefix"

  private val form = new EoriNumberFormProvider()()

  private val validPrefixes = Seq("GB", "gb", "Gb", "gB", "XI", "xi", "Xi", "xI")
  private val prefixGen     = Gen.oneOf(validPrefixes)

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
      alphaNumericRegex,
      maxEoriNumberLength,
      invalidKey = invalidCharactersKey
    )

    "must not bind strings with correct prefix and suffix but over max length" in {
      val expectedError = FormError(fieldName, maxLengthKey, Seq(maxEoriNumberLength))

      val gen = for {
        prefix <- prefixGen
        suffix <- stringsLongerThan(maxEoriNumberLength - prefix.length, Gen.numChar)
      } yield prefix + suffix

      forAll(gen) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }

    "must not bind strings with correct prefix and suffix but under min length" in {
      val expectedError = FormError(fieldName, minLengthKey, Seq(minEoriNumberLength))

      val gen = for {
        prefix <- prefixGen
        suffix <- stringsWithMaxLength(minEoriNumberLength - prefix.length - 1, Gen.numChar)
      } yield prefix + suffix

      forAll(gen) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }

    "must not bind strings with correct prefix but invalid suffix" in {
      val expectedError = FormError(fieldName, invalidFormatKey, Seq(eoriNumberRegex))

      val gen = for {
        prefix <- prefixGen
        suffix <- stringsLongerThan(maxEoriNumberLength - prefix.length, Gen.alphaNumChar)
      } yield prefix + suffix

      forAll(gen) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }

    "must not bind strings with wrong prefix" in {
      val expectedError = FormError(fieldName, invalidPrefixFormatKey, Seq(eoriNumberPrefixRegex))

      val gen = for {
        prefix <- stringsOfLength(2, Gen.alphaChar).retryUntil(!validPrefixes.contains(_))
        suffix <- stringsWithLengthInRange(minEoriNumberLength - prefix.length, maxEoriNumberLength - prefix.length, Gen.numChar)
      } yield prefix + suffix

      forAll(gen) {
        invalidString =>
          val result: Field = form.bind(Map(fieldName -> invalidString)).apply(fieldName)
          result.errors must contain(expectedError)
      }
    }

    "must remove spaces on bound strings" in {
      val result = form.bind(Map(fieldName -> " GB 123 456 789 123 "))
      result.errors mustEqual Nil
      result.get mustEqual "GB123456789123"
    }

    "must not accept HTML/JavaScript characters" in {
      val result = form.bind(Map(fieldName -> "GB000142;<script>print()</script>"))
      result.errors must contain(FormError(fieldName, invalidCharactersKey, Seq(alphaNumericRegex)))
    }

  }
}
