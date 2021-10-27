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

import base.SpecBase
import controllers.routes
import models.{CheckMode, Mode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.{AccessCodePage, EoriNumberPage, GuaranteeReferenceNumberPage}
import uk.gov.hmrc.viewmodels.MessageInterpolators
import uk.gov.hmrc.viewmodels.SummaryList.{Action, Key, Row, Value}

class CheckYourAnswersHelperSpec extends SpecBase {

  "eoriNumber" - {

    "return None" - {
      "EoriNumberPage undefined" in {

        val answers = emptyUserAnswers

        val helper = new CheckYourAnswersHelper(answers, CheckMode)
        val result = helper.eoriNumber

        result mustBe None
      }
    }

    "return Some(row)" - {
      "EoriNumberPage defined" in {

        forAll(arbitrary[Mode], arbitrary[String]) {
          (mode, answer) =>
            val answers: UserAnswers = emptyUserAnswers.set(EoriNumberPage, answer).success.value

            val helper = new CheckYourAnswersHelper(answers, mode)
            val result = helper.eoriNumber

            val label = msg"eoriNumber.checkYourAnswersLabel"

            result mustBe Some(
              Row(
                key = Key(label, classes = Seq("govuk-!-width-one-half")),
                value = Value(lit"$answer"),
                actions = List(
                  Action(
                    content = msg"site.edit",
                    href = routes.EoriNumberController.onPageLoad(mode).url,
                    visuallyHiddenText = Some(label)
                  )
                )
              )
            )
        }
      }
    }
  }

  "guaranteeReferenceNumber" - {

    "return None" - {
      "GuaranteeReferenceNumberPage undefined" in {

        val answers = emptyUserAnswers

        val helper = new CheckYourAnswersHelper(answers, CheckMode)
        val result = helper.guaranteeReferenceNumber

        result mustBe None
      }
    }

    "return Some(row)" - {
      "GuaranteeReferenceNumberPage defined" in {

        forAll(arbitrary[Mode], arbitrary[String]) {
          (mode, answer) =>
            val answers: UserAnswers = emptyUserAnswers.set(GuaranteeReferenceNumberPage, answer).success.value

            val helper = new CheckYourAnswersHelper(answers, mode)
            val result = helper.guaranteeReferenceNumber

            val label = msg"guaranteeReferenceNumber.checkYourAnswersLabel"

            result mustBe Some(
              Row(
                key = Key(label, classes = Seq("govuk-!-width-one-half")),
                value = Value(lit"$answer"),
                actions = List(
                  Action(
                    content = msg"site.edit",
                    href = routes.GuaranteeReferenceNumberController.onPageLoad(mode).url,
                    visuallyHiddenText = Some(label)
                  )
                )
              )
            )
        }
      }
    }
  }

  "accessCode" - {

    "return None" - {
      "AccessCodePage undefined" in {

        val answers = emptyUserAnswers

        val helper = new CheckYourAnswersHelper(answers, CheckMode)
        val result = helper.accessCode

        result mustBe None
      }
    }

    "return Some(row)" - {
      "AccessCodePage defined" in {

        forAll(arbitrary[Mode], arbitrary[String]) {
          (mode, answer) =>
            val answers: UserAnswers = emptyUserAnswers.set(AccessCodePage, answer).success.value

            val helper = new CheckYourAnswersHelper(answers, mode)
            val result = helper.accessCode

            val label = msg"accessCode.checkYourAnswersLabel"

            result mustBe Some(
              Row(
                key = Key(label, classes = Seq("govuk-!-width-one-half")),
                value = Value(lit"$answer"),
                actions = List(
                  Action(
                    content = msg"site.edit",
                    href = routes.AccessCodeController.onPageLoad(mode).url,
                    visuallyHiddenText = Some(label)
                  )
                )
              )
            )
        }
      }
    }
  }

}
