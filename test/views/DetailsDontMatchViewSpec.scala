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

package views

import play.twirl.api.HtmlFormat
import views.behaviours.FeedbackViewBehaviours
import views.html.DetailsDontMatchView

class DetailsDontMatchViewSpec extends FeedbackViewBehaviours {

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[DetailsDontMatchView].apply()(fakeRequest, messages)

  override val prefix: String = "detailsDontMatch"

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithLink(
    "try-again",
    "Check your details and try again",
    controllers.routes.CheckYourAnswersController.onPageLoad().url
  )

  behave like pageWithContent("p", "If your details are correct, contact the NCTS helpdesk (opens in a new tab).")

  behave like pageWithLink(
    "contact",
    "contact the NCTS helpdesk (opens in a new tab)",
    frontendAppConfig.nctsEnquiriesUrl
  )

  behave like pageWithFeedback()
}
