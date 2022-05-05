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

package views

import play.twirl.api.HtmlFormat
import views.behaviours.ViewBehaviours
import views.html.NotFoundView

class NotFoundViewSpec extends ViewBehaviours {

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[NotFoundView].apply()(fakeRequest, messages)

  override val prefix: String = "pageNotFound"

  private val contactUrl = "https://www.gov.uk/government/organisations/hm-revenue-customs/contact/new-computerised-transit-system-enquiries"

  behave like pageWithTitle()

  behave like pageWithBackLink

  behave like pageWithHeading()

  behave like pageWithContent("p", "If you typed the web address, check it is correct.")
  behave like pageWithContent("p", "If you pasted the web address, check you copied the entire address.")
  behave like pageWithPartialContent("p", "If the web address is correct or you selected a link or button, ")
  behave like pageWithLink(
    "contact",
    "contact the New Computerised Transit System (NCTS) helpdesk if you need to speak to someone about your guarantee balance (opens in a new tab)",
    contactUrl
  )
}
