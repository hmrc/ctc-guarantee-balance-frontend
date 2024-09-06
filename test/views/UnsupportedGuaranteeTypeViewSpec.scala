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
import views.behaviours.ViewBehaviours
import views.html.UnsupportedGuaranteeTypeView

class UnsupportedGuaranteeTypeViewSpec extends ViewBehaviours {

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[UnsupportedGuaranteeTypeView].apply()(fakeRequest, messages)

  override val prefix: String = "unsupportedGuaranteeType"

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithContent("p", "We can `only` get the balance for:")

  behave like pageWithList(
    "govuk-list--bullet",
    "comprehensive guarantee",
    "guarantee waiver",
    "individual guarantee with multiple usage"
  )

  behave like pageWithPartialContent("p", "You can")
  behave like pageWithLink(
    "checkDetails-link",
    "change the reference of the guarantee you are checking",
    controllers.routes.CheckYourAnswersController.onPageLoad().url
  )
  behave like pageWithPartialContent("p", "or you can start again.")

  behave like pageWithSubmitButton("Start again")
}
