/*
 * Copyright 2023 HM Revenue & Customs
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

import models.Referral
import models.Referral._
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.twirl.api.HtmlFormat
import views.behaviours.PanelViewBehaviours
import views.html.BalanceConfirmationView

class BalanceConfirmationViewSpec extends PanelViewBehaviours {

  private val balance  = Gen.numStr.sample.value
  private val referral = arbitrary[Option[Referral]].sample.value.map(_.toString)

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[BalanceConfirmationView].apply(balance, referral)(fakeRequest, messages)

  override val prefix: String = "balanceConfirmation"

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithPanel(balance)

  "when NCTS referral" - {
    val view = injector.instanceOf[BalanceConfirmationView].apply(balance, Some(NCTS.toString))(fakeRequest, messages)
    val doc  = parseView(view)

    behave like pageWithContent(doc, "p", "You can:")
    behave like pageWithLinkedList(
      doc,
      "govuk-list--bullet",
      (
        "check-another-guarantee-balance",
        "check another guarantee balance",
        controllers.routes.BalanceConfirmationController.checkAnotherGuaranteeBalance().url
      ),
      (
        "manage-transit-movements",
        "manage your transit movements",
        controllers.routes.BalanceConfirmationController.manageTransitMovements().url
      )
    )
  }

  "when GovUK or no referral" - {
    val referral = Gen.oneOf(Some(GovUK.toString), None).sample.value
    val view     = injector.instanceOf[BalanceConfirmationView].apply(balance, referral)(fakeRequest, messages)
    val doc      = parseView(view)

    behave like pageWithPartialContent(doc, "p", "You can")
    behave like pageWithLink(
      doc,
      "check-another-guarantee-balance",
      "check another guarantee balance",
      controllers.routes.BalanceConfirmationController.checkAnotherGuaranteeBalance().url
    )
  }
}
