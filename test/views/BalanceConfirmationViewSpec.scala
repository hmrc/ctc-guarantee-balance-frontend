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

import models.Referral._
import models.backend.BalanceRequestSuccess
import models.values.CurrencyCode
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.Json

class BalanceConfirmationViewSpec extends SingleViewSpec("balanceConfirmation.njk") {

  "must render balance confirmation" in {
    forAll(arbitrary[Int]) {
      balance =>
        val balanceForDisplay = BalanceRequestSuccess(balance, CurrencyCode("GBP")).toString

        val json = Json.obj(
          "balance" -> balanceForDisplay
        )

        val doc = renderDocument(json).futureValue

        val balanceConfirmation = doc.select(".govuk-panel--confirmation").text()

        balanceConfirmation must include("balanceConfirmation.heading")
        balanceConfirmation must include(balanceForDisplay)
    }
  }

  private val fakeUrl = "/fake-url"

  "must render links" - {

    "when user has come from GOV.UK" in {

      val json = Json.obj(
        "referral"                        -> GovUK,
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
        "referral"                        -> NCTS,
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
        expectedHref = controllers.routes.BalanceConfirmationController.manageTransitMovements().url
      )
    }

  }

  "must not render links" - {
    "when referral is None (i.e. cookie not set)" in {

      val json = Json.obj(
        "referral"                        -> None,
        "checkAnotherGuaranteeBalanceUrl" -> fakeUrl
      )

      val doc = renderDocument(json).futureValue

      doc.text() mustNot include("balanceConfirmation.fromGovUk.p")
      doc.text() mustNot include("balanceConfirmation.fromNcts.p")

      assertPageDoesNotHaveLink(
        doc = doc,
        id = "check-another-guarantee-balance"
      )

      assertPageDoesNotHaveLink(
        doc = doc,
        id = "manage-transit-movements"
      )
    }
  }

}
