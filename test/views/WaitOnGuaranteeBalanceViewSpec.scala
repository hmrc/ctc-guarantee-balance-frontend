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

class WaitOnGuaranteeBalanceViewSpec extends SingleViewSpec("waitOnGuaranteeBalance.njk") {

  "must render correct heading" in {
    assertPageTitleEqualsMessage(doc, "waitOnGuaranteeBalance.heading")
  }

  "must render waitOnGuaranteeBalance prelink text" in {
    assertContainsText(doc, "waitOnGuaranteeBalance.checkDetails.prelink")
  }

  "display link with id checkDetails-link" in {
    assertPageHasLink(doc, "checkDetails-link", "waitOnGuaranteeBalance.checkDetails.link", routes.CheckYourAnswersController.onPageLoad().url)
  }

  "must render waitOnGuaranteeBalance postlink text" in {
    assertContainsText(doc, "waitOnGuaranteeBalance.checkDetails.postlink")
  }

  "behave like a page with a submit button" in {
    assertPageHasButton(doc, "site.tryAgain")
  }
}
