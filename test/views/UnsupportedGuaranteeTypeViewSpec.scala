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

import controllers.routes

class UnsupportedGuaranteeTypeViewSpec extends SingleViewSpec("unsupportedGuaranteeType.njk") {

  "must render correct heading" in {
    assertPageTitleEqualsMessage(doc, "unsupportedGuaranteeType.heading")
  }

  "must render paragraph" in {
    assertContainsText(doc, "unsupportedGuaranteeType.paragraph")
  }

  "must render paragraph.bullet1" in {
    assertContainsText(doc, "unsupportedGuaranteeType.paragraph.bullet1")
  }

  "must render paragraph.bullet2" in {
    assertContainsText(doc, "unsupportedGuaranteeType.paragraph.bullet2")
  }

  "must render paragrap.bullet3" in {
    assertContainsText(doc, "unsupportedGuaranteeType.paragraph.bullet3")
  }

  "display link with id checkDetails-link" in {
    assertPageHasLink(doc, "checkDetails-link", "unsupportedGuaranteeType.checkDetails.link", routes.CheckYourAnswersController.onPageLoad().url)
  }

  "behave like a page with a submit button" in {
    assertPageHasButton(doc, "site.startAgain")
  }
}
