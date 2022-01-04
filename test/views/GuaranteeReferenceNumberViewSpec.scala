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

import org.jsoup.nodes.Element

class GuaranteeReferenceNumberViewSpec extends SingleViewSpec("guaranteeReferenceNumber.njk") {

  val input: Element = doc.getElementsByClass("govuk-input").first()

  "must render paragraph text" in {
    assertContainsText(doc, "guaranteeReferenceNumber.paragraph")
  }

  "must have correct width class" in {
    input.hasClass("govuk-input--width-20") mustBe true
  }

  "must have correct ID" in {
    input.id() mustEqual "value"
  }

  "must render a continue button" in {
    assertPageHasButton(doc, "site.continue")
  }

}
