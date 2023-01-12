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

package views.behaviours

import org.jsoup.nodes.Document

trait ErrorSummaryViewBehaviours[T] {
  this: QuestionViewBehaviours[T] =>

  def pageWithErrorSummary(): Unit =
    "when rendered with an error" - {
      "must show an error summary" in {
        assertRenderedByClass(docWithError(), "govuk-error-summary")
      }

      "must show an error in the value field's label" in {
        val errorSpan = docWithError().getElementsByClass("govuk-error-message").first
        assertElementContainsText(errorSpan, s"${messages("error.title.prefix")} ${messages(errorMessage)}")
      }
    }

  def pageWithoutErrorSummary(document: Document = doc): Unit =
    "must not render an error summary" in {
      assertNotRenderedByClass(document, "govuk-error-summary")
    }
}
