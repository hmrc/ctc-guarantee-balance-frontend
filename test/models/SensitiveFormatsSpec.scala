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

import base.{AppWithDefaultMockFixtures, SpecBase}
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.running

class SensitiveFormatsSpec extends SpecBase {

  "JsObject" - {
    val encryptedValue = "WFYrOuMf6WHDjHooyzED80QIGXMTPSHEjc3Kl8jPFRJFtHWV"
    val decryptedValue = Json.obj()

    "reads" - {
      "when encryption enabled" - {
        "must read an encrypted value" in {
          val sensitiveFormats = new SensitiveFormats(true)
          val result           = Json.toJson(encryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
          result mustEqual decryptedValue
        }

        "must read a decrypted value" in {
          val sensitiveFormats = new SensitiveFormats(true)
          val result           = Json.toJson(decryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
          result mustEqual decryptedValue
        }
      }

      "when encryption disabled" - {
        "must read an encrypted value" in {
          val sensitiveFormats = new SensitiveFormats(false)
          val result           = Json.toJson(encryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
          result mustEqual decryptedValue
        }

        "must read a decrypted value" in {
          val sensitiveFormats = new SensitiveFormats(false)
          val result           = Json.toJson(decryptedValue).as[JsObject](sensitiveFormats.jsObjectReads)
          result mustEqual decryptedValue
        }
      }
    }

    "writes" - {
      "when encryption enabled" - {
        "must write and encrypt the value" in {
          val sensitiveFormats = new SensitiveFormats(true)
          val result           = Json.toJson(decryptedValue)(sensitiveFormats.jsObjectWrites)
          result must not be decryptedValue
        }
      }

      "encryption disabled" - {
        "must write and not encrypt the value" in {
          val sensitiveFormats = new SensitiveFormats(false)
          val result           = Json.toJson(decryptedValue)(sensitiveFormats.jsObjectWrites)
          result mustEqual decryptedValue
        }
      }
    }
  }
}
