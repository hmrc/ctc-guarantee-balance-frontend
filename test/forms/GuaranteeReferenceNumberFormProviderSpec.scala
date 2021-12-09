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

import forms.Constants._
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class GuaranteeReferenceNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey  = "guaranteeReferenceNumber.error.required"
  val maxLengthKey = "guaranteeReferenceNumber.error.length"
  val invalidKey   = "guaranteeReferenceNumber.error.invalid"

  val form = new GuaranteeReferenceNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form = form,
      fieldName = fieldName,
      validDataGenerator = stringsWithMaxLength(maxGuaranteeReferenceNumberLength)
    )

    behave like fieldWithMaxLength(
      form = form,
      fieldName = fieldName,
      maxLength = maxGuaranteeReferenceNumberLength,
      lengthError = FormError(fieldName, maxLengthKey, Seq(maxGuaranteeReferenceNumberLength))
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
      gen = stringsOfLength(maxGuaranteeReferenceNumberLength),
      invalidKey = invalidKey
    )

    "must remove spaces on bound strings" in {
      val result = form.bind(Map(fieldName -> " 123 456 "))
      result.errors mustEqual Nil
      result.get mustEqual "123456"
    }

  }
}
