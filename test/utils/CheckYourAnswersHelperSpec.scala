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

package utils

import base.SpecBase
import controllers.routes
import forms.Constants.accessCodeLength
import models.{CheckMode, Mode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.{AccessCodePage, EoriNumberPage, GuaranteeReferenceNumberPage}
import uk.gov.hmrc.govukfrontend.views.Aliases._
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.html.components.{ActionItem, Actions}

class CheckYourAnswersHelperSpec extends SpecBase {

  "eoriNumber" - {

    "must return None" - {
      "when EoriNumberPage undefined" in {

        val answers = emptyUserAnswers

        val helper = new CheckYourAnswersHelper(answers, CheckMode)
        val result = helper.eoriNumber

        result mustBe None
      }
    }

    "must return Some(row)" - {
      "when EoriNumberPage defined" in {

        forAll(arbitrary[Mode], arbitrary[String]) {
          (mode, answer) =>
            val answers: UserAnswers = emptyUserAnswers.setValue(EoriNumberPage, answer)

            val helper = new CheckYourAnswersHelper(answers, mode)
            val result = helper.eoriNumber

            result mustBe Some(
              SummaryListRow(
                key = Key("EORI number".toText),
                value = Value(answer.toText),
                actions = Some(
                  Actions(
                    items = List(
                      ActionItem(
                        content = "Change".toText,
                        href = routes.EoriNumberController.onPageLoad(mode).url,
                        visuallyHiddenText = Some("EORI number"),
                        attributes = Map("id" -> "change-eori-number")
                      )
                    )
                  )
                )
              )
            )
        }
      }
    }
  }

  "guaranteeReferenceNumber" - {

    "must return None" - {
      "when GuaranteeReferenceNumberPage undefined" in {

        val answers = emptyUserAnswers

        val helper = new CheckYourAnswersHelper(answers, CheckMode)
        val result = helper.guaranteeReferenceNumber

        result mustBe None
      }
    }

    "must return Some(row)" - {

      "for V1" - {

        "when GuaranteeReferenceNumberPage defined" in {

          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, answer) =>
              val answers: UserAnswers = emptyUserAnswers.setValue(GuaranteeReferenceNumberPage, answer)

              val helper = new CheckYourAnswersHelper(answers, mode)
              val result = helper.guaranteeReferenceNumber

              result mustBe Some(
                SummaryListRow(
                  key = Key("Guarantee reference number".toText),
                  value = Value(answer.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.GuaranteeReferenceNumberController.onPageLoad(mode).url,
                          visuallyHiddenText = Some("guarantee reference number"),
                          attributes = Map("id" -> "change-guarantee-reference-number")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }

      "for V2" - {

        "when GuaranteeReferenceNumberPage defined" in {

          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, answer) =>
              val answers: UserAnswers = emptyUserAnswers.setValue(GuaranteeReferenceNumberPage, answer)

              val helper = new CheckYourAnswersHelper(answers, mode)
              val result = helper.guaranteeReferenceNumberV2

              result mustBe Some(
                SummaryListRow(
                  key = Key("Guarantee Reference Number (GRN)".toText),
                  value = Value(answer.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.GuaranteeReferenceNumberController.onPageLoad(mode).url,
                          visuallyHiddenText = Some("Guarantee Reference Number (GRN)"),
                          attributes = Map("id" -> "change-guarantee-reference-number")
                        )
                      )
                    )
                  )
                )
              )
          }
        }
      }
    }
  }

  "accessCode" - {

    "must return None" - {
      "when AccessCodePage undefined" in {

        val answers = emptyUserAnswers

        val helper = new CheckYourAnswersHelper(answers, CheckMode)
        val result = helper.accessCode

        result mustBe None
      }
    }

    "must return Some(row)" - {
      "when AccessCodePage defined" in {

        forAll(arbitrary[Mode], stringsOfLength(accessCodeLength)) {
          (mode, answer) =>
            val answers: UserAnswers = emptyUserAnswers.setValue(AccessCodePage, answer)

            val helper = new CheckYourAnswersHelper(answers, mode)
            val result = helper.accessCode

            result mustBe Some(
              SummaryListRow(
                key = Key("Access code".toText),
                value = Value("••••".toText),
                actions = Some(
                  Actions(
                    items = List(
                      ActionItem(
                        content = "Change".toText,
                        href = routes.AccessCodeController.onPageLoad(mode).url,
                        visuallyHiddenText = Some("access code"),
                        attributes = Map("id" -> "change-access-code")
                      )
                    )
                  )
                )
              )
            )
        }
      }
    }
  }

}
