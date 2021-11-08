/*
 * Copyright 2021 HM Revenue & Customs
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

  "must have paragraph text and link" in {
    assertContainsText(doc, "detailsDontMatch.youMust")

    assertPageHasLink(
      doc = doc,
      id = "try-again",
      expectedText = "detailsDontMatch.checkYourAnswers",
      expectedHref = controllers.routes.CheckYourAnswersController.onPageLoad().url
    )
  }

}
