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

package utils

import controllers.routes
import models.{Mode, UserAnswers}
import pages._
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels._

class CheckYourAnswersHelper(userAnswers: UserAnswers, mode: Mode) {

  def eoriNumber: Option[Row] = userAnswers.get(EoriNumberPage) map {
    answer =>
      Row(
        key = Key(msg"eoriNumber.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.EoriNumberController.onPageLoad(mode).url,
            visuallyHiddenText = Some(msg"eoriNumber.checkYourAnswersLabel")
          )
        )
      )
  }

  def guaranteeReferenceNumber: Option[Row] = userAnswers.get(GuaranteeReferenceNumberPage) map {
    answer =>
      Row(
        key = Key(msg"guaranteeReferenceNumber.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.GuaranteeReferenceNumberController.onPageLoad(mode).url,
            visuallyHiddenText = Some(msg"guaranteeReferenceNumber.checkYourAnswersLabel")
          )
        )
      )
  }

  def accessCode: Option[Row] = userAnswers.get(AccessCodePage) map {
    answer =>
      Row(
        key = Key(msg"accessCode.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.AccessCodeController.onPageLoad(mode).url,
            visuallyHiddenText = Some(msg"accessCode.checkYourAnswersLabel")
          )
        )
      )
  }

}
