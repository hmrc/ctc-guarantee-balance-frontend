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
import play.api.libs.json.{JsString, Json}

class ModeSpec extends SpecBase {

  "NormalMode" - {

    val mode: Mode = NormalMode

    "must convert to string" in {
      mode.toString mustEqual "NormalMode"
    }

    "must serialise correctly" in {
      Json.toJson(mode) mustEqual JsString("NormalMode")
    }

    "must return correct jsLiteral" in {
      Mode.jsLiteral.to(mode) mustEqual """"NormalMode""""
    }
  }

  "CheckMode" - {

    val mode: Mode = CheckMode

    "must convert to string" in {
      mode.toString mustEqual "CheckMode"
    }

    "must serialise correctly" in {
      Json.toJson(mode) mustEqual JsString("CheckMode")
    }

    "must return correct jsLiteral" in {
      Mode.jsLiteral.to(mode) mustEqual """"CheckMode""""
    }
  }

}
