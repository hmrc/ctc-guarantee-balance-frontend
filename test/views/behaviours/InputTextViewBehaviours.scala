/*
 * Copyright 2022 HM Revenue & Customs
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

package views.behaviours

import generators.Generators
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import viewModels.InputSize

trait InputTextViewBehaviours[T] extends QuestionViewBehaviours[T] with Generators {

  implicit val arbitraryT: Arbitrary[T]
  private lazy val validValue: T = arbitrary[T].sample.value

  def pageWithInputText(inputFieldClassSize: Option[InputSize] = None): Unit =
    "page with an input text field" - {
      "when rendered" - {

        "must contain an input field" in {
          assert(getElementsByTag(doc, "input").size > 0)
        }

        "must have the correct class govuk-input" in {
          assert(getElementById(doc, "value").hasClass("govuk-input"))
        }

        "must have the correct classes" in {
          inputFieldClassSize match {
            case Some(sizeClass) => assert(getElementById(doc, "value").hasClass(sizeClass.className))
            case None            => assert(getElementById(doc, "value").classNames().size == 1)
          }
        }

        "must not render an error summary" in {
          assertNotRenderedById(doc, "error-summary_header")
        }
      }

      "when rendered with a valid value" - {
        val docWithFilledForm = parseView(applyView(form.fill(validValue)))

        "include the form's value in the value input" in {
          docWithFilledForm.getElementById("value").attr("value") mustBe validValue.toString
        }

        "must not render an error summary" in {
          assertNotRenderedById(docWithFilledForm, "error-summary_header")
        }
      }

      "when rendered with an error" - {
        "must show an error summary" in {
          assertRenderedById(docWithError(), "error-summary-title")
        }

        "must show an error in the value field's label" in {
          val errorSpan = docWithError().getElementsByClass("govuk-error-message").first
          assertElementContainsText(errorSpan, s"${messages("error.title.prefix")} ${messages(errorMessage)}")
        }
      }
    }

}
