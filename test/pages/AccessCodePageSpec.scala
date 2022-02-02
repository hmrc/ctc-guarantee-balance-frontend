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

import java.util.UUID

import models.UserAnswers
import models.values.BalanceId
import pages.behaviours.PageBehaviours
import play.api.libs.json.Json

class AccessCodePageSpec extends PageBehaviours {

  private val access: String = "access"
  private val expectedUuid   = UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4")
  private val balanceId      = BalanceId(expectedUuid)

  // format: off
  val baseUserAnswers: UserAnswers = UserAnswers("id", Json.obj())
    .set(AccessCodePage, access).success.value
  // format: on

  "AccessCodePage" - {

    beRetrievable[String](AccessCodePage)

    beSettable[String](AccessCodePage)

    beRemovable[String](AccessCodePage)

    "cleanup" - {

      "must remove BalanceId when AccessCode changes" in {
        val answersWithBalanceId = baseUserAnswers.set(BalanceIdPage, balanceId).success.value
        answersWithBalanceId.get(BalanceIdPage).isDefined mustEqual true

        val result = answersWithBalanceId.set(AccessCodePage, "newValue")
        result.success.value.get(BalanceIdPage).isDefined mustEqual false
      }

      "must NOT remove BalanceId when AccessCode hasn't changed" in {
        val answersWithBalanceId = baseUserAnswers.set(BalanceIdPage, balanceId).success.value
        answersWithBalanceId.get(BalanceIdPage).isDefined mustEqual true

        val result = answersWithBalanceId.set(AccessCodePage, access)
        result.success.value.get(BalanceIdPage).isDefined mustEqual true
      }
    }
  }
}
