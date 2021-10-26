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

import forms.Constants.{alphaNumericRegex, maxGuaranteeReferenceNumberLength}
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class GuaranteeReferenceNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "guaranteeReferenceNumber.error.required"
  val lengthKey   = "guaranteeReferenceNumber.error.length"
  val invalidKey  = "guaranteeReferenceNumber.error.invalid"

  val form = new GuaranteeReferenceNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxGuaranteeReferenceNumberLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxGuaranteeReferenceNumberLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxGuaranteeReferenceNumberLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldWithInvalidCharacters(
      form,
      fieldName,
      alphaNumericRegex,
      stringsOfLength(maxGuaranteeReferenceNumberLength),
      invalidKey
    )

  }
}
