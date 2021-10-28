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

import play.api.libs.json.Json

class WaitOnGuaranteeBalanceViewSpec extends SingleViewSpec("waitOnGuaranteeBalance.njk", hasSignOutLink = true) {

  "must render correct heading" in {
    val json = Json.obj()
    val doc  = renderDocument(json).futureValue

    assertPageTitleEqualsMessage(doc, "waitOnGuaranteeBalance.heading")
  }

  "must render waitOnGuaranteeBalance text" in {
    val json = Json.obj()
    val doc  = renderDocument(json).futureValue

    assertContainsText(doc, "waitOnGuaranteeBalance.paragraph")
  }
}
