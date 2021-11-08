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

import org.jsoup.nodes.Element

class AccessCodeViewSpec extends SingleViewSpec("accessCode.njk") {

  val input: Element = doc.getElementsByClass("govuk-input").first()

  "must have password-type input" in {
    input.attr("type") mustEqual "password"
  }

  "must have correct width class" in {
    input.hasClass("govuk-input--width-5") mustBe true
  }

  "must have correct ID" in {
    input.id() mustEqual "accessCode"
  }

  "must render hint text" in {
    val hint = doc.getElementsByClass("govuk-hint").first()
    hint.text() mustEqual "accessCode.hintText"
  }

  "must have autocomplete off" in {
    input.attr("autocomplete") mustBe "off"
  }

  "must render a continue button" in {
    assertPageHasButton(doc, "site.continue")
  }

}
