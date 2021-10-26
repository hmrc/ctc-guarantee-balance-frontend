package forms

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class GuaranteeReferenceNumberFormProviderSpec extends StringFieldBehaviours {

  val requiredKey = "guaranteeReferenceNumber.error.required"
  val lengthKey = "guaranteeReferenceNumber.error.length"
  val maxLength = 24

  val form = new GuaranteeReferenceNumberFormProvider()()

  ".value" - {

    val fieldName = "value"

    behave like fieldThatBindsValidData(
      form,
      fieldName,
      stringsWithMaxLength(maxLength)
    )

    behave like fieldWithMaxLength(
      form,
      fieldName,
      maxLength = maxLength,
      lengthError = FormError(fieldName, lengthKey, Seq(maxLength))
    )

    behave like mandatoryField(
      form,
      fieldName,
      requiredError = FormError(fieldName, requiredKey)
    )
  }
}
