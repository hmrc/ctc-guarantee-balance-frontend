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

package models

import base.SpecBase

import java.time.LocalDateTime

class TimestampSpec extends SpecBase {

  "apply" - {
    "must convert instant to timestamp with formatted date and time" - {
      "when am" in {
        val dateTime  = LocalDateTime.of(2024, 10, 22, 9, 59, 32)
        val timestamp = Timestamp(dateTime)
        timestamp.date mustBe "22 October 2024"
        timestamp.time mustBe "09:59"
      }

      "when pm" in {
        val dateTime  = LocalDateTime.of(2023, 6, 15, 13, 9, 17)
        val timestamp = Timestamp(dateTime)
        timestamp.date mustBe "15 June 2023"
        timestamp.time mustBe "13:09"
      }
    }
  }

}
