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
import play.api.libs.json.{JsString, Json}

class SubmissionModeSpec extends SpecBase {

  "SubmitMode" - {

    val mode: SubmissionMode = SubmitMode

    "must convert to string" in {
      mode.toString mustEqual "SubmitMode"
    }

    "must serialise correctly" in {
      Json.toJson(mode) mustEqual JsString("SubmitMode")
    }

    "must return correct jsLiteral" in {
      SubmissionMode.jsLiteral.to(mode) mustEqual """"SubmitMode""""
    }
  }

  "PollMode" - {

    val mode: SubmissionMode = PollMode

    "must convert to string" in {
      mode.toString mustEqual "PollMode"
    }

    "must serialise correctly" in {
      Json.toJson(mode) mustEqual JsString("PollMode")
    }

    "must return correct jsLiteral" in {
      SubmissionMode.jsLiteral.to(mode) mustEqual """"PollMode""""
    }
  }

}
