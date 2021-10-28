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

import models.Balance
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

class BalanceConfirmationViewSpec extends SingleViewSpec("balanceConfirmation.njk") {

  "must render balance confirmation" in {
    forAll(arbitrary[Int]) {
      balance =>
        val balanceForDisplay = Balance(balance).forDisplay

        val json = Json.obj(
          "balance" -> balanceForDisplay
        )

        val doc = renderDocument(json).futureValue

        val balanceConfirmation = doc.select(".govuk-panel--confirmation").text()

        balanceConfirmation must include("balanceConfirmation.heading")
        balanceConfirmation must include(balanceForDisplay)
    }
  }

  "must render links" - {

    val fakeUrl = "/fake-url"

    "when user has come from GOV.UK" in {

      val json = Json.obj(
        "isNctsUser"                      -> false,
        "checkAnotherGuaranteeBalanceUrl" -> fakeUrl
      )

      val doc = renderDocument(json).futureValue

      doc.text() must include("balanceConfirmation.fromGovUk.p")

      assertPageHasLink(
        doc = doc,
        id = "check-another-guarantee-balance",
        expectedText = "balanceConfirmation.fromGovUk.link",
        expectedHref = fakeUrl
      )
    }

    "when user has come from NCTS" in {

      val json = Json.obj(
        "isNctsUser"                      -> true,
        "checkAnotherGuaranteeBalanceUrl" -> fakeUrl
      )

      val doc = renderDocument(json).futureValue

      doc.text() must include("balanceConfirmation.fromNcts.p")

      assertPageHasLink(
        doc = doc,
        id = "check-another-guarantee-balance",
        expectedText = "balanceConfirmation.fromNcts.link1",
        expectedHref = fakeUrl
      )

      assertPageHasLink(
        doc = doc,
        id = "manage-transit-movements",
        expectedText = "balanceConfirmation.fromNcts.link2",
        expectedHref = frontendAppConfig.manageTransitMovementsUrl
      )
    }

  }

}
