/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import base.{AppWithDefaultMockFixtures, SpecBase}

class DateTimeServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val service = app.injector.instanceOf[DateTimeService]

  "DateTimeService" - {
    "now" - {
      "must return current instant" in {
        val instant1 = service.now
        val instant2 = service.now

        instant2.isAfter(instant1).mustBe(true)
      }
    }

    "currentDateTime" - {
      "must return current date/time" in {
        val instant1 = service.currentDateTime
        val instant2 = service.currentDateTime

        instant2.isAfter(instant1).mustBe(true)
      }
    }
  }
}
