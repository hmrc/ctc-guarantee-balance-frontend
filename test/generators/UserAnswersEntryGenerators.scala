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

import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators {
  self: Generators =>

  implicit lazy val arbitraryAccessCodeUserAnswersEntry: Arbitrary[(AccessCodePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AccessCodePage.type]
        value <- nonEmptyString.map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEoriNumberUserAnswersEntry: Arbitrary[(EoriNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EoriNumberPage.type]
        value <- nonEmptyString.map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryGuaranteeReferenceNumberUserAnswersEntry: Arbitrary[(GuaranteeReferenceNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[GuaranteeReferenceNumberPage.type]
        value <- nonEmptyString.map(Json.toJson(_))
      } yield (page, value)
    }

}
