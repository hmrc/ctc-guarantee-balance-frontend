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

package utils

import controllers.routes
import models.{Mode, UserAnswers}
import pages._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow

class CheckYourAnswersHelper(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages) extends AnswersHelper(userAnswers) {

  def eoriNumber: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = EoriNumberPage,
      formatAnswer = formatAsText,
      prefix = "eoriNumber",
      id = Some("change-eori-number"),
      call = routes.EoriNumberController.onPageLoad(mode)
    )

  def guaranteeReferenceNumber: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = GuaranteeReferenceNumberPage,
      formatAnswer = formatAsText,
      prefix = "guaranteeReferenceNumber",
      id = Some("change-guarantee-reference-number"),
      call = routes.GuaranteeReferenceNumberController.onPageLoad(mode)
    )

  def accessCode: Option[SummaryListRow] =
    getAnswerAndBuildRow[String](
      page = AccessCodePage,
      formatAnswer = formatAsPassword,
      prefix = "accessCode",
      id = Some("change-access-code"),
      call = routes.AccessCodeController.onPageLoad(mode)
    )

}
