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

import forms.Constants.{alphaNumericWithSpacesRegex, maxGuaranteeReferenceNumberLength, minGuaranteeReferenceNumberLength}
import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class GuaranteeReferenceNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey  = "guaranteeReferenceNumber.error.required"
  val maxLengthKey = "guaranteeReferenceNumber.error.maxLength"
  val minLengthKey = "guaranteeReferenceNumber.error.minLength"
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

    behave like fieldWithMinLength(
      form = form,
      fieldName = fieldName,
      minLength = minGuaranteeReferenceNumberLength,
      lengthError = FormError(fieldName, minLengthKey, Seq(minGuaranteeReferenceNumberLength))
    )

    behave like mandatoryField(
      form = form,
      fieldName = fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )

    behave like fieldThatDoesNotBindInvalidData(
      form = form,
      fieldName = fieldName,
      regex = alphaNumericWithSpacesRegex,
      gen = stringsOfLength(maxGuaranteeReferenceNumberLength),
      invalidKey = invalidKey
    )

    behave like fieldThatIgnoresSpaces(
      form = form,
      fieldName = fieldName,
      regex = alphaNumericWithSpacesRegex,
      minLength = minGuaranteeReferenceNumberLength,
      maxLength = maxGuaranteeReferenceNumberLength,
      lengthError = FormError(fieldName, minLengthKey, Seq(minGuaranteeReferenceNumberLength))
    )

  }
}
