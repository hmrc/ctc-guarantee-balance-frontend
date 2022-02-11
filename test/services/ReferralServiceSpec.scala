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

package services

import base.{AppWithDefaultMockFixtures, SpecBase}
import models.Referral
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest

class ReferralServiceSpec extends SpecBase with ScalaCheckPropertyChecks with AppWithDefaultMockFixtures {

  private val referralService = new ReferralService()

  "getReferralFromSession" - {

    "when referral exists" - {
      "must return Some value" in {
        forAll(arbitrary[String]) {
          value =>
            implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest.withSession(Referral.key -> value)
            referralService.getReferralFromSession.get mustEqual value
        }
      }
    }

    "when referral does not exist" - {
      "must return None" in {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest
        referralService.getReferralFromSession mustNot be(defined)
      }
    }
  }

  "setReferralInSession" - {
    "must set referral in session" in {
      forAll(arbitrary[Referral]) {
        referral =>
          implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest
          val resultBefore                                          = Ok
          resultBefore.session.get(Referral.key) mustNot be(defined)
          val resultAfter = referralService.setReferralInSession(resultBefore, referral)
          resultAfter.session.get(Referral.key).get mustEqual referral.toString
      }
    }
  }
}
