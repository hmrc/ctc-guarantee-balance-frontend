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
import controllers.routes._
import models.{CheckMode, UserAnswers}
import pages._
import play.api.i18n.Messages
import uk.gov.hmrc.viewmodels._
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels.Text.Literal

class CheckYourAnswersHelper(userAnswers: UserAnswers)(implicit messages: Messages) {

  def eoriNumber: Option[Row] = userAnswers.get(EoriNumberPage) map {
    answer =>
      Row(
        key = Key(msg"eoriNumber.checkYourAnswersLabel", classes = Seq("govuk-!-width-one-half")),
        value = Value(lit"$answer"),
        actions = List(
          Action(
            content = msg"site.edit",
            href = routes.EoriNumberController.onPageLoad(CheckMode).url,
            visuallyHiddenText = Some(msg"site.edit.hidden".withArgs(msg"eoriNumber.checkYourAnswersLabel"))
          )
        )
      )
  }
}
