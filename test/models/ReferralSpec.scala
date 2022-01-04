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

package models

import base.SpecBase
import models.Referral._
import org.scalacheck.Arbitrary.arbitrary
import play.api.libs.json.{JsString, Json}

class ReferralSpec extends SpecBase {

  private val referralKey: String = "referral"

  "GovUK" - {

    val referral: Referral = GovUK

    "must convert to string correctly" in {
      referral.toString mustEqual "govuk"
    }

    "must serialise correctly" in {
      Json.toJson(referral) mustEqual JsString("govuk")
    }

    "must deserialise correctly" in {
      JsString("govuk").as[Referral] mustEqual referral
    }

    "must return correct jsLiteral" in {
      Referral.jsLiteral.to(referral) mustEqual """"govuk""""
    }

    "must bind correctly" - {

      "when lower case" in {
        Referral.queryStringBinder.bind(referralKey, Map(referralKey -> Seq("govuk"))) mustBe Some(Right(referral))
      }

      "when not lower case" in {
        Referral.queryStringBinder.bind(referralKey, Map(referralKey -> Seq("gOvUk"))) mustBe Some(Right(referral))
      }
    }

    "must unbind correctly" in {
      Referral.queryStringBinder.unbind(referralKey, referral) mustBe s"$referralKey=govuk"
    }
  }

  "NCTS" - {

    val referral: Referral = NCTS

    "must convert to string correctly" in {
      referral.toString mustEqual "ncts"
    }

    "must serialise correctly" in {
      Json.toJson(referral) mustEqual JsString("ncts")
    }

    "must deserialise correctly" in {
      JsString("ncts").as[Referral] mustEqual referral
    }

    "must return correct jsLiteral" in {
      Referral.jsLiteral.to(referral) mustEqual """"ncts""""
    }

    "must bind correctly" - {

      "when lower case" in {
        Referral.queryStringBinder.bind(referralKey, Map(referralKey -> Seq("ncts"))) mustBe Some(Right(referral))
      }

      "when not lower case" in {
        Referral.queryStringBinder.bind(referralKey, Map(referralKey -> Seq("nCtS"))) mustBe Some(Right(referral))
      }
    }

    "must unbind correctly" in {
      Referral.queryStringBinder.unbind(referralKey, referral) mustBe s"$referralKey=ncts"
    }
  }

  "must not bind invalid query" in {
    forAll(
      arbitrary[String].suchThat(
        x => !Referral.values.map(_.toString).contains(x)
      )
    ) {
      str =>
        Referral.queryStringBinder.bind(referralKey, Map(referralKey -> Seq(str))) mustBe Some(Left(s"Invalid Referral Type: ${str.toLowerCase}"))
    }
  }

}
