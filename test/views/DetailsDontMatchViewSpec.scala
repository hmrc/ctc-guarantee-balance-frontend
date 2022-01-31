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

class DetailsDontMatchViewSpec extends SingleViewSpec("detailsDontMatch.njk") {

  "must have correct heading" in {
    assertPageTitleEqualsMessage(doc, "detailsDontMatch.heading")
  }

  "must have 'try again' paragraph text and link" in {
    assertContainsText(doc, "detailsDontMatch.p1")

    assertPageHasLink(
      doc = doc,
      id = "try-again",
      expectedText = "detailsDontMatch.p1.a",
      expectedHref = controllers.routes.CheckYourAnswersController.onPageLoad().url
    )
  }

  "must have 'contact helpdesk' paragraph text and link" in {
    assertContainsText(doc, "detailsDontMatch.p2")

    assertPageHasLink(
      doc = doc,
      id = "contact",
      expectedText = "detailsDontMatch.p2.a",
      expectedHref = frontendAppConfig.nctsEnquiriesUrl
    )
  }

}
