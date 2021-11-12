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

package controllers.actions

import base.{AppWithDefaultMockFixtures, SpecBase}
import models.Referral
import org.scalacheck.Arbitrary.arbitrary
import play.api.mvc._
import play.api.test.Helpers._

import scala.concurrent.ExecutionContext.Implicits.global

class ReferralActionSpec extends SpecBase with AppWithDefaultMockFixtures {

  private class Harness(referralAction: ReferralAction) {

    def test(): Action[AnyContent] = referralAction {
      _ => Results.Ok
    }
  }

  implicit val bodyParsers: BodyParsers.Default = injector.instanceOf[BodyParsers.Default]

  "Referral Action" - {

    "when referral provided" - {
      "must store referral in a cookie" in {

        forAll(arbitrary[Referral]) {
          referral =>
            val referralAction = new ReferralAction(Some(referral))

            val harness = new Harness(referralAction)
            val result  = harness.test()(fakeRequest)

            status(result) mustBe OK
            cookies(result) must contain(Cookie(Referral.cookieName, referral.toString))
        }
      }
    }

    "when referral not provided" - {
      "must not store referral in a cookie" in {

        val referralAction = new ReferralAction(None)

        val harness = new Harness(referralAction)
        val result  = harness.test()(fakeRequest)

        status(result) mustBe OK
        cookies(result).map(_.name) mustNot contain(Referral.cookieName)
      }
    }
  }
}
