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

package pages

import models.UserAnswers
import models.values.BalanceId
import pages.behaviours.PageBehaviours
import play.api.libs.json.Json

import java.util.UUID

class EoriNumberPageSpec extends PageBehaviours {

  private val taxId: String = "taxId"
  private val expectedUuid  = UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4")
  private val balanceId     = BalanceId(expectedUuid)

  val baseUserAnswers: UserAnswers = UserAnswers("id", Json.obj())
    .setValue(EoriNumberPage, taxId)

  "EoriNumberPage" - {

    beRetrievable[String](EoriNumberPage)

    beSettable[String](EoriNumberPage)

    beRemovable[String](EoriNumberPage)

    "cleanup" - {

      "must remove BalanceId when EORI changes" in {
        val answersWithBalanceId = baseUserAnswers.setValue(BalanceIdPage, balanceId)
        answersWithBalanceId.get(BalanceIdPage).isDefined mustEqual true

        val result = answersWithBalanceId.setValue(EoriNumberPage, "newValue")
        result.get(BalanceIdPage).isDefined mustEqual false
      }

      "must NOT remove BalanceId when EORI hasn't changed" in {
        val answersWithBalanceId = baseUserAnswers.setValue(BalanceIdPage, balanceId)
        answersWithBalanceId.get(BalanceIdPage).isDefined mustEqual true

        val result = answersWithBalanceId.setValue(EoriNumberPage, taxId)
        result.get(BalanceIdPage).isDefined mustEqual true
      }
    }
  }
}
