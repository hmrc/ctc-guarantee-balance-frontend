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

import base.{AppWithDefaultMockFixtures, SpecBase}
import pages.{AccessCodePage, GuaranteeReferenceNumberPage}
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import viewModels.CheckYourAnswersViewModel.CheckYourAnswersViewModelProvider

class CheckYourAnswersViewModelSpec extends SpecBase with AppWithDefaultMockFixtures {

  val viewModelProvider: CheckYourAnswersViewModelProvider = injector.instanceOf[CheckYourAnswersViewModelProvider]

  "when user answers are empty" - {

    val result = viewModelProvider(emptyUserAnswers)

    "must have no section title" in {
      result.section.sectionTitle mustNot be(defined)
    }

    "must have no rows" in {
      result.section.rows mustBe empty
    }
  }

  "when user answers are not empty" - {

    val grn  = "grn"
    val code = "••••"

    val userAnswers = emptyUserAnswers
      .setValue(GuaranteeReferenceNumberPage, grn)
      .setValue(AccessCodePage, code)

    val result = viewModelProvider(userAnswers)

    "must have no section title" in {
      result.section.sectionTitle mustNot be(defined)
    }

    "must have 2 rows" in {
      result.section.rows.size mustBe 2
    }

    Seq(grn, code).zipWithIndex.foreach {
      case (value, index) =>
        s"when row ${index + 1}" - {
          val row = result.section.rows(index)

          "must correspond to the correct value" in {
            row.value.content mustEqual Text(value)
          }

          "must have 1 action" in {
            row.actions.size mustBe 1
          }

          "must be using CheckMode" in {
            row.actions.get.items.foreach(_.href must include("change"))
          }
        }
    }
  }

}
