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
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class GuaranteeReferenceNumberFormProviderSpec extends StringFieldBehaviours {

  "GuaranteeReferenceNumberFormProvider" - {

    val maxLength = maxGuaranteeReferenceNumberLength
    val minLength = minGuaranteeReferenceNumberLength

    val requiredKey  = "guaranteeReferenceNumber.error.required"
    val maxLengthKey = "guaranteeReferenceNumber.error.length"
    val minLengthKey = "guaranteeReferenceNumber.error.length"
    val invalidKey   = "guaranteeReferenceNumber.error.invalid"

    val form = new GuaranteeReferenceNumberFormProvider()()

    ".value" - {

      val fieldName = "value"

      behave like fieldThatBindsValidData(
        form = form,
        fieldName = fieldName,
        validDataGenerator = stringsWithMaxLength(maxLength)
      )

      behave like fieldWithMaxLength(
        form = form,
        fieldName = fieldName,
        maxLength = maxLength,
        lengthError = FormError(fieldName, maxLengthKey, Seq(maxLength))
      )

      behave like fieldWithMinLength(
        form = form,
        fieldName = fieldName,
        minLength = minLength,
        lengthError = FormError(fieldName, minLengthKey, Seq(minLength))
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
        gen = stringsOfLength(maxLength),
        invalidKey = invalidKey
      )

      "must remove spaces on bound strings" in {
        val result = form.bind(Map(fieldName -> " 01GB1234567 890120 "))
        result.errors mustEqual Nil
        result.get mustEqual "01GB1234567890120"
      }

      "must not accept HTML/JavaScript characters" in {
        val result = form.bind(Map(fieldName -> "Test;<p>Hello</p>"))
        result.errors must contain(FormError(fieldName, invalidKey, Seq(alphaNumericRegex)))
      }

    }
  }
}
