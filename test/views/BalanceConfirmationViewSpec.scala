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

import models.Referral._
import models.{Referral, Timestamp}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.twirl.api.HtmlFormat
import views.behaviours.PanelViewBehaviours
import views.html.BalanceConfirmationView

class BalanceConfirmationViewSpec extends PanelViewBehaviours {

  private val balance   = nonEmptyString.sample.value
  private val timestamp = arbitrary[Timestamp].sample.value
  private val referral  = arbitrary[Option[Referral]].sample.value.map(_.toString)

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[BalanceConfirmationView].apply(balance, timestamp, referral)(fakeRequest, messages)

  override val prefix: String = "balanceConfirmation"

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithPanel(s"$balance as of ${timestamp.date} at ${timestamp.time}")

  behave like pageWithContent(doc, "p", "This is your guarantee limit minus the liability amounts for your open movements.")

  "when NCTS referral" - {
    val view = injector.instanceOf[BalanceConfirmationView].apply(balance, timestamp, Some(NCTS.toString))(fakeRequest, messages)
    val doc  = parseView(view)

    behave like pageWithLink(
      doc,
      "check-another-guarantee-balance",
      "Check another guarantee balance",
      controllers.routes.BalanceConfirmationController.checkAnotherGuaranteeBalance().url
    )

    behave like pageWithLink(
      doc,
      "manage-transit-movements",
      "Manage your transit movements",
      controllers.routes.BalanceConfirmationController.manageTransitMovements().url
    )
  }

  "when GovUK or no referral" - {
    val referral = Gen.oneOf(Some(GovUK.toString), None).sample.value
    val view     = injector.instanceOf[BalanceConfirmationView].apply(balance, timestamp, referral)(fakeRequest, messages)
    val doc      = parseView(view)

    behave like pageWithLink(
      doc,
      "check-another-guarantee-balance",
      "Check another guarantee balance",
      controllers.routes.BalanceConfirmationController.checkAnotherGuaranteeBalance().url
    )
  }

  behave like pageWithContent("h2", "Before you go")

  behave like pageWithLink(
    "feedback",
    "Take a short survey",
    "http://localhost:9514/feedback/check-transit-guarantee-balance"
  )
}
