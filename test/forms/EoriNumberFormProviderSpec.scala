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

package forms

import forms.Constants.{alphaNumericRegex, eoriNumberRegex, maxEoriNumberLength}
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError
import wolfendale.scalacheck.regexp.RegexpGen

class EoriNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey          = "eoriNumber.error.required"
  val lengthKey            = "eoriNumber.error.length"
  val invalidCharactersKey = "eoriNumber.error.invalidCharacters"
  val invalidFormatKey     = "eoriNumber.error.invalidFormat"

  val form = new EoriNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form = form,
      fieldName = fieldName,
      validDataGenerator = stringsWithMaxLength(maxEoriNumberLength)
    )

    behave like fieldWithMaxLength(
      form = form,
      fieldName = fieldName,
      maxLength = maxEoriNumberLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxEoriNumberLength))
    )

    behave like mandatoryField(
      form = form,
      fieldName = fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidCharacters(
      form = form,
      fieldName = fieldName,
      regex = alphaNumericRegex,
      gen = stringsOfLength(maxEoriNumberLength),
      invalidKey = invalidCharactersKey
    )

    behave like fieldWithInvalidCharacters(
      form = form,
      fieldName = fieldName,
      regex = eoriNumberRegex,
      gen = RegexpGen.from(alphaNumericRegex.replace("*", s"{$maxEoriNumberLength}")),
      invalidKey = invalidFormatKey
    )

  }
}
