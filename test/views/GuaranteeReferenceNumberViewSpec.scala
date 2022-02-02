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

  "must render hint text" in {
    assertContainsText(doc, messages("guaranteeReferenceNumber.paragraph1"))
    assertContainsText(doc, messages("guaranteeReferenceNumber.bullet1"))
    assertContainsText(doc, messages("guaranteeReferenceNumber.bullet2"))
    assertContainsText(doc, messages("guaranteeReferenceNumber.bullet3"))
    assertContainsText(doc, messages("guaranteeReferenceNumber.paragraph2"))
  }

  "must have correct width class" in {
    assert(input.hasClass("govuk-input--width-20"))
  }

  "must have correct ID" in {
    input.id() mustEqual "value"
  }

  "must render a continue button" in {
    assertPageHasButton(doc, "site.continue")
  }

  "must render correct title" in {
    assertPageHasTitle(doc, "guaranteeReferenceNumber")
  }

  "must render correct heading" in {
    assertPageTitleEqualsMessage(doc, "guaranteeReferenceNumber.heading")
  }

  "must render correct label" in {
    val label = doc.getElementsByClass("govuk-label").first()
    assert(label.hasClass("govuk-label--m"))
    label.text() mustBe messages("guaranteeReferenceNumber.label")
  }

}
