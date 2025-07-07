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

trait FeedbackViewBehaviours extends ViewBehaviours {

  def pageWithFeedback(): Unit =
    "page with a feedback summary" - {
      "when rendered" - {
        "must have a h2" - {
          behave like pageWithContent("h2", "Before you go")
        }

        "must have a paragraph" - {
          behave like pageWithContent(
            tag = "p",
            expectedText = "Your feedback helps us make our service better. Take a short survey to share your feedback on this service."
          )
        }

        "must have a link" - {
          behave like pageWithLink(
            id = "feedback",
            expectedText = "Take a short survey",
            expectedHref = frontendAppConfig.feedbackUrl
          )
        }
      }
    }
}
