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

package controllers.actions

import base.SpecBase
import models.Referral
import org.apache.pekko.stream.testkit.NoMaterializer
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import play.api.mvc.*
import play.api.test.Helpers.*
import services.ReferralService

import scala.concurrent.ExecutionContext.Implicits.global

class ReferralActionSpec extends SpecBase {

  private class Harness(referralAction: ReferralAction) {

    def test(): Action[AnyContent] = referralAction {
      _ => Results.Ok
    }
  }

  private val mockReferralService = mock[ReferralService]

  "Referral Action" - {

    "when referral provided" - {
      "must store referral in session" in {

        forAll(arbitrary[Referral]) {
          referral =>
            when(mockReferralService.setReferralInSession(any(), any())(any()))
              .thenReturn(Results.Ok.withSession(Referral.key -> referral.toString))

            val referralAction = new ReferralAction(Some(referral))(mockReferralService)

            val harness = new Harness(referralAction)
            val result  = harness.test()(fakeRequest)

            status(result) mustEqual OK
            session(result).get(Referral.key).get mustEqual referral.toString
        }
      }
    }

    "when referral not provided" - {
      "must not store referral in session" in {

        val referralAction = new ReferralAction(None)(mockReferralService)

        val harness = new Harness(referralAction)
        val result  = harness.test()(fakeRequest)

        status(result) mustEqual OK
        session(result).get(Referral.key) must not be defined
      }
    }
  }
}
