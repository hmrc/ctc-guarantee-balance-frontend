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

import play.api.libs.json.Json

class TechnicalDifficultiesViewSpec extends SingleViewSpec("technicalDifficulties.njk") {

  "must render tryAgain text" in {
    assertContainsText(doc, "technicalDifficulties.tryAgain")
  }

  "must render savedAnswers text" in {
    assertContainsText(doc, "technicalDifficulties.savedAnswers")
  }

  "display link with id contact-link" in {
    val url = frontendAppConfig.nctsEnquiriesUrl
    val json = Json.obj(
      "contactUrl" -> url
    )
    val doc = renderDocument(json).futureValue

    assertPageHasLink(doc, "contact-link", "technicalDifficulties.contact.link", url)
  }
}
