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

package viewModels

import models.{CheckMode, UserAnswers}
import play.api.i18n.Messages
import utils.CheckYourAnswersHelper

case class CheckYourAnswersViewModelV1(section: Section)

object CheckYourAnswersViewModelV1 {

  class CheckYourAnswersViewModelProviderV1 {

    def apply(userAnswers: UserAnswers)(implicit messages: Messages): CheckYourAnswersViewModelV1 = {
      val helper = new CheckYourAnswersHelper(userAnswers, CheckMode)

      CheckYourAnswersViewModelV1(
        Section(
          Seq(
            helper.eoriNumber,
            helper.guaranteeReferenceNumber,
            helper.accessCode
          ).flatten
        )
      )
    }

  }

}
