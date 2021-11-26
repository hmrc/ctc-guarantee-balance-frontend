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

case class UnsuccessfulBalanceAuditModel(transaction: String,
                                         audit: String,
                                         eoriNumber: String,
                                         guaranteeReferenceNumber: String,
                                         accessCode: String,
                                         status: Int,
                                         errorMessage: String
) extends JsonAuditModel {

  override val transactionName: String = transaction
  override val auditType: String       = audit

  override val detail: JsValue = Json.obj(
    "Eori Number"                -> eoriNumber.toString,
    "Guarantee Reference Number" -> guaranteeReferenceNumber.toString,
    "Access Code"                -> accessCode.toString,
    "status"                     -> status,
    "Error Message"              -> errorMessage
  )
}

object UnsuccessfulBalanceAuditModel {

  def build(transaction: String,
            audit: String,
            eoriNumber: String,
            guaranteeReferenceNumber: String,
            accessCode: String,
            status: Int,
            errorMessage: String
  ): UnsuccessfulBalanceAuditModel =
    UnsuccessfulBalanceAuditModel(transaction, audit, eoriNumber, guaranteeReferenceNumber, accessCode, status, errorMessage)
}
