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

package components

import a11ySpecBase.A11ySpecBase
import forms.AccessCodeFormProvider
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.twirl.api.Html
import viewModels.components.InputTextViewModel._
import views.html.components.InputText
import views.html.templates.MainTemplate

class InputTextSpec extends A11ySpecBase {

  "the 'input text' component" must {
    val template  = app.injector.instanceOf[MainTemplate]
    val component = app.injector.instanceOf[InputText]

    val title      = nonEmptyString.sample.value
    val label      = nonEmptyString.sample.value
    val inputClass = Gen.option(Gen.alphaNumStr).sample.value
    val hint       = Gen.option(nonEmptyString).sample.value
    val prefix     = Gen.option(Gen.alphaNumStr).sample.value
    val suffix     = Gen.option(Gen.alphaNumStr).sample.value
    val inputMode  = Gen.option(Gen.alphaNumStr).sample.value
    val (inputType, autocomplete) = Gen
      .oneOf(
        ("tel", Gen.option(Gen.const("tel")).sample.value),
        ("text", Gen.option(Gen.oneOf("address-line1", "address-line2", "postal-code")).sample.value)
      )
      .sample
      .value
    val additionalHtml = arbitrary[Html].sample.value
    val form           = new AccessCodeFormProvider()()

    "pass accessibility checks" when {

      "ordinary text input" in {
        val content = template.apply(title) {
          component.apply(
            form("value"),
            OrdinaryTextInput(title),
            inputClass,
            hint,
            prefix,
            suffix,
            autocomplete,
            inputMode,
            inputType
          )
        }
        content.toString() must passAccessibilityChecks
      }

      "text input with hidden label" in {
        val content = template.apply(title) {
          component
            .apply(
              form("value"),
              TextInputWithHiddenLabel(title, Some(label), additionalHtml),
              inputClass,
              hint,
              prefix,
              suffix,
              autocomplete,
              inputMode,
              inputType
            )
        }
        content.toString() must passAccessibilityChecks
      }

      "text input with statement heading" in {
        val content = template.apply(title) {
          component
            .apply(
              form("value"),
              TextInputWithStatementHeading(title, label, additionalHtml),
              inputClass,
              hint,
              prefix,
              suffix,
              autocomplete,
              inputMode,
              inputType
            )
        }
        content.toString() must passAccessibilityChecks
      }

      "address text input" in {
        val content = template.apply(title) {
          component
            .apply(
              form("value"),
              AddressTextInput(label),
              inputClass,
              hint,
              prefix,
              suffix,
              autocomplete,
              inputMode,
              inputType
            )
            .withHeading(title)
        }
        content.toString() must passAccessibilityChecks
      }
    }
  }
}
