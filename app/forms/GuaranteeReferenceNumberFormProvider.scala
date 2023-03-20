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
import forms.mappings.Mappings
import play.api.data.Form

sealed trait GuaranteeReferenceNumberFormProvider extends Mappings {

  val prefix: String

  val maxGuaranteeReferenceNumberLength: Int

  def apply(): Form[String] =
    Form(
      "value" -> textWithSpacesRemoved(s"$prefix.error.required")
        .verifying(
          forms.StopOnFirstFail[String](
            maxLength(maxGuaranteeReferenceNumberLength, s"$prefix.error.length"),
            regexp(alphaNumericRegex, s"$prefix.error.invalid")
          )
        )
    )
}

class V1GuaranteeReferenceNumberFormProvider extends GuaranteeReferenceNumberFormProvider {

  override val maxGuaranteeReferenceNumberLength: Int = Constants.maxGuaranteeReferenceNumberLength

  override val prefix: String = "guaranteeReferenceNumber"
}

class V2GuaranteeReferenceNumberFormProvider extends GuaranteeReferenceNumberFormProvider {

  override val maxGuaranteeReferenceNumberLength: Int = Constants.maxGuaranteeReferenceNumberLengthV2

  override val prefix: String = "guaranteeReferenceNumber.v2"
}
