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

package navigation

import base.SpecBase
import controllers.routes
import generators.Generators
import models._
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages._

class NavigatorSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      val mode: Mode = NormalMode

      "must go from EORI number page to GRN page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(EoriNumberPage, mode, answers)
              .mustBe(routes.GuaranteeReferenceNumberController.onPageLoad(mode))
        }
      }

      "must go from GRN page to Access code page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(GuaranteeReferenceNumberPage, mode, answers)
              .mustBe(routes.AccessCodeController.onPageLoad(mode))
        }
      }

      "must go from Access code page to Check your answers page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AccessCodePage, mode, answers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "must go from a page that doesn't exist in the route map to Start" - {

        case object UnknownPage extends Page

        "when referral exists in user answers" in {

          forAll(arbitrary[UserAnswers], arbitrary[Referral]) {
            (answers, referral) =>
              val updatedAnswers = answers.set(ReferralPage, referral).success.value
              navigator
                .nextPage(UnknownPage, mode, updatedAnswers)
                .mustBe(routes.StartController.start(referral))
          }
        }

        "when referral doesn't exist in user answers" in {

          forAll(arbitrary[UserAnswers]) {
            answers =>
              navigator
                .nextPage(UnknownPage, mode, answers)
                .mustBe(routes.StartController.start())
          }
        }
      }
    }

    "in Check mode" - {

      val mode: Mode = CheckMode

      "must go from EORI number page to GRN page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(EoriNumberPage, mode, answers)
              .mustBe(routes.GuaranteeReferenceNumberController.onPageLoad(mode))
        }
      }

      "must go from GRN page to Access code page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(GuaranteeReferenceNumberPage, mode, answers)
              .mustBe(routes.AccessCodeController.onPageLoad(mode))
        }
      }

      "must go from Access code page to Check your answers page" in {

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(AccessCodePage, mode, answers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "must go from a page that doesn't exist in the route map to CheckYourAnswers" in {

        case object UnknownPage extends Page

        forAll(arbitrary[UserAnswers]) {
          answers =>
            navigator
              .nextPage(UnknownPage, mode, answers)
              .mustBe(routes.CheckYourAnswersController.onPageLoad())
        }
      }

    }
  }
}
