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

import models.values.BalanceId
import org.scalacheck.Arbitrary.arbitrary
import play.twirl.api.HtmlFormat
import views.behaviours.FeedbackViewBehaviours
import views.html.TryAgainView

class TryAgainViewSpec extends FeedbackViewBehaviours {

  private val balanceId = arbitrary[Option[BalanceId]].sample.value.map(_.value)

  override def view: HtmlFormat.Appendable =
    injector.instanceOf[TryAgainView].apply(balanceId)(fakeRequest, messages)

  override val prefix: String = "tryAgain"

  behave like pageWithTitle()

  behave like pageWithoutBackLink()

  behave like pageWithHeading()

  behave like pageWithPartialContent("p", s"Wait for 60 seconds - then")

  behave like pageWithLink(
    "check-details",
    "check your details are correct and try again",
    controllers.routes.CheckYourAnswersController.onPageLoad().url
  )

  "when balance ID is not defined" - {
    val view = injector.instanceOf[TryAgainView].apply(None)(fakeRequest, messages)
    val doc  = parseView(view)
    behave like pageWithNoInput(doc)
  }

  behave like pageWithFeedback()
}
