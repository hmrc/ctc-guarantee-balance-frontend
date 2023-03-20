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

import models.Mode
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Request
import play.twirl.api.HtmlFormat
import viewModels.Section
import views.html._

import java.util.UUID
import javax.inject.Inject

sealed trait ViewProvider {

  def guaranteeReferenceNumberView(
    form: Form[String],
    mode: Mode
  )(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable

  def accessCodeView(
    form: Form[String],
    mode: Mode
  )(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable

  def balanceConfirmationView(balance: String, referral: Option[String])(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable

  def detailsDoNotMatch()(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable

  def couldNotCheckBalance(balanceId: Option[UUID])(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable

  def checkYourAnswers(sections: Seq[Section])(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable
}

class V1ViewProvider @Inject() (
  guaranteeReferenceNumberView: GuaranteeReferenceNumberView,
  accessCodeView: AccessCodeView,
  balanceConfirmationView: BalanceConfirmationView,
  detailsDoNotMatchV1: DetailsDontMatchView,
  tryAgainView: TryAgainView,
  checkYourAnswersView: CheckYourAnswersView
) extends ViewProvider {

  override def guaranteeReferenceNumberView(
    form: Form[String],
    mode: Mode
  )(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable =
    guaranteeReferenceNumberView.apply(form, mode)

  override def accessCodeView(
    form: Form[String],
    mode: Mode
  )(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable =
    accessCodeView.apply(form, mode)

  override def balanceConfirmationView(balance: String, referral: Option[String])(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable =
    balanceConfirmationView.apply(balance, referral)

  override def detailsDoNotMatch()(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable =
    detailsDoNotMatchV1.apply()

  override def couldNotCheckBalance(balanceId: Option[UUID])(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable =
    tryAgainView.apply(balanceId)

  override def checkYourAnswers(sections: Seq[Section])(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable =
    checkYourAnswersView.apply(sections)

}

class V2ViewProvider @Inject() (
  guaranteeReferenceNumberView: GuaranteeReferenceNumberViewV2,
  accessCodeView: AccessCodeViewV2,
  balanceConfirmationView: BalanceConfirmationViewV2,
  detailsDoNotMatchV2: DetailsDontMatchViewV2,
  tryAgainViewV2: TryAgainViewV2,
  checkYourAnswersViewV2: CheckYourAnswersViewV2
) extends ViewProvider {

  override def guaranteeReferenceNumberView(
    form: Form[String],
    mode: Mode
  )(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable =
    guaranteeReferenceNumberView.apply(form, mode)

  override def accessCodeView(
    form: Form[String],
    mode: Mode
  )(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable =
    accessCodeView.apply(form, mode)

  override def balanceConfirmationView(balance: String, referral: Option[String])(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable =
    balanceConfirmationView.apply(balance, referral)

  override def detailsDoNotMatch()(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable =
    detailsDoNotMatchV2.apply()

  override def couldNotCheckBalance(balanceId: Option[UUID])(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable =
    tryAgainViewV2.apply(balanceId)

  override def checkYourAnswers(sections: Seq[Section])(implicit request: Request[_], messages: Messages): HtmlFormat.Appendable =
    checkYourAnswersViewV2.apply(sections)
}
