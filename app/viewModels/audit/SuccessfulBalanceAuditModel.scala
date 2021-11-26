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

import play.api.libs.json.{JsValue, Json}
import services.JsonAuditModel

case class SuccessfulBalanceAuditModel(eoriNumber: String, guaranteeReferenceNumber: String, accessCode: String, status: Int, balance: String)
    extends JsonAuditModel {

  override val transactionName: String = "Successful Balance"
  override val auditType: String       = "Successful Balance Audit"

  override val detail: JsValue = Json.obj(
    "Eori Number"                -> eoriNumber,
    "Guarantee Reference Number" -> guaranteeReferenceNumber,
    "Access Code"                -> accessCode,
    "status"                     -> status,
    "Balance"                    -> balance
  )
}

object SuccessfulBalanceAuditModel {

  def build(eoriNumber: String, guaranteeReferenceNumber: String, accessCode: String, status: Int, balance: String): SuccessfulBalanceAuditModel =
    SuccessfulBalanceAuditModel(eoriNumber, guaranteeReferenceNumber, accessCode, status, balance)
}
