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

package generators

import models.*
import models.values.BalanceId
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}

import java.time.LocalDateTime
import java.util.UUID

trait ModelGenerators {

  implicit lazy val arbitraryMode: Arbitrary[Mode] =
    Arbitrary {
      Gen.oneOf(NormalMode, CheckMode)
    }

  implicit lazy val arbitraryReferral: Arbitrary[Referral] =
    Arbitrary {
      Gen.oneOf(Referral.values)
    }

  implicit lazy val arbitraryBalanceId: Arbitrary[BalanceId] =
    Arbitrary {
      arbitrary[UUID].map(BalanceId(_))
    }

  implicit lazy val arbitraryTimestamp: Arbitrary[Timestamp] =
    Arbitrary {
      Timestamp(LocalDateTime.now())
    }
}
