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

class EoriNumberViewSpec extends SingleViewSpec("eoriNumber.njk") {

  "must have correct width class" in {
    val doc   = renderDocument().futureValue
    val input = doc.getElementsByClass("govuk-input").first()
    input.hasClass("govuk-input--width-20") mustBe true
  }

  "must have correct ID" in {
    val doc   = renderDocument().futureValue
    val input = doc.getElementsByClass("govuk-input").first()
    input.id() mustEqual "eoriNumber"
  }

}