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

package viewModels.audit

import java.time.LocalDateTime

import play.api.libs.json.{JsValue, Json}
import services.JsonAuditModel

case class UnsuccessfulBalanceAuditModel(eoriNumber: String, guaranteeReferenceNumber: String, accessCode: String, status: Int, errorMessage: String)
    extends JsonAuditModel {

  override val transactionName: String = "Unsuccessful Balance"
  override val auditType: String       = "Unsuccessful Balance Audit"

  override val detail: JsValue = Json.obj(
    "Eori Number"                -> eoriNumber.toString,
    "Guarantee Reference Number" -> guaranteeReferenceNumber.toString,
    "Access Code"                -> accessCode.toString,
    "status"                     -> status,
    "Error Message"              -> errorMessage
  )
}

object UnsuccessfulBalanceAuditModel {

  def build(eoriNumber: String, guaranteeReferenceNumber: String, accessCode: String, status: Int, errorMessage: String): UnsuccessfulBalanceAuditModel =
    UnsuccessfulBalanceAuditModel(eoriNumber, guaranteeReferenceNumber, accessCode, status, errorMessage)
}

/*
date and time of attempt
all user info we have from GG login
EORI submitted
GRN submitted
access code submitted
status of what is returned
What balance is returned
What user displayed error has been returned (rate limited, details don't match, guarantee type not supported)
Plain English reason of what the error that was sent (incorrect EORI, incorrect GRN, incorrect access code, EORI/GRN do not match, guarantee type not supported)
Comment that the user doesn't see what value was incorrect, just that the details submitted do not match
 */
