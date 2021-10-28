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

package models

import base.SpecBase

// scalastyle:off magic.number
class BalanceSpec extends SpecBase {

  ".forDisplay" - {

    "must display a balance of 1 as £1" in {
      val balance = Balance(1)
      balance.forDisplay mustEqual "£1"
    }

    "must display a balance of 10 as £10" in {
      val balance = Balance(10)
      balance.forDisplay mustEqual "£10"
    }

    "must display a balance of 1000 as £1,000" in {
      val balance = Balance(1000)
      balance.forDisplay mustEqual "£1,000"
    }

    "must display a balance of 10000 as £10,000" in {
      val balance = Balance(10000)
      balance.forDisplay mustEqual "£10,000"
    }

    "must display a balance of 1000000 as £1,000,000" in {
      val balance = Balance(1000000)
      balance.forDisplay mustEqual "£1,000,000"
    }

  }
}
// scalastyle:on magic.number
