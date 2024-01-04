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
import models.RichString
import play.api.data.Form

import javax.inject.Inject

class EoriNumberFormProvider @Inject() extends Mappings {

  def apply(): Form[String] =
    Form(
      "value" -> adaptedText("eoriNumber.error.required")(_.removeSpaces())
        .verifying(
          forms.StopOnFirstFail[String](
            regexp(alphaNumericRegex, "eoriNumber.error.invalidCharacters"),
            regexp(eoriNumberPrefixRegex, "eoriNumber.error.prefix"),
            regexp(eoriNumberRegex, "eoriNumber.error.invalidFormat"),
            minLength(minEoriNumberLength, "eoriNumber.error.minLength"),
            maxLength(maxEoriNumberLength, "eoriNumber.error.maxLength")
          )
        )
    )
}
