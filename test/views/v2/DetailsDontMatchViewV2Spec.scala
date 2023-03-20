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

package views.v2

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.v2.DetailsDontMatchViewV2

class DetailsDontMatchViewV2Spec extends ViewBehaviours {

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[DetailsDontMatchViewV2].apply()(fakeRequest, messages)

  override val prefix: String = "detailsDontMatch.v2"

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithLink(
    "try-again",
    "Check your details and try again",
    controllers.routes.CheckYourAnswersControllerV2.onPageLoad().url
  )

  behave like pageWithPartialContent("p", "If your details are correct,")
  behave like pageWithLink(
    "contact",
    "contact the NCTS helpdesk (opens in a new tab)",
    frontendAppConfig.nctsEnquiriesUrl
  )
}
