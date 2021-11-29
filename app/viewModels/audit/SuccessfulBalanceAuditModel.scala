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

import org.joda.time.LocalDateTime
import play.api.libs.json.{JsValue, Json}
import services.JsonAuditModel
import viewModels.audit.AuditConstants._

case class SuccessfulBalanceAuditModel(eoriNumber: String,
                                       guaranteeReferenceNumber: String,
                                       accessCode: String,
                                       internalId: String,
                                       dateTime: LocalDateTime,
                                       status: Int,
                                       balance: String
) extends JsonAuditModel {

  override val transactionName: String = AUDIT_SUBMIT_BALANCE_TRANSACTION_SUCCESS
  override val auditType: String       = AUDIT_SUBMIT_BALANCE_REQUEST

  override val detail: JsValue = Json.obj(
    AUDIT_FIELD_EORI_NUMBER    -> eoriNumber,
    AUDIT_FIELD_GRN_NUMBER     -> guaranteeReferenceNumber,
    AUDIT_FIELD_ACCESS_CODE    -> accessCode,
    AUDIT_FIELD_GG_INTERNAL_ID -> internalId,
    AUDIT_FIELD_DATE_TIME      -> dateTime.toString,
    AUDIT_FIELD_STATUS         -> status,
    AUDIT_FIELD_BALANCE        -> balance
  )
}

object SuccessfulBalanceAuditModel {

  def build(  eoriNumber: String,
            guaranteeReferenceNumber: String,
            accessCode: String,
            internalId: String,
            dateTime: LocalDateTime,
            status: Int,
            balance: String
  ): SuccessfulBalanceAuditModel =
    SuccessfulBalanceAuditModel(eoriNumber, guaranteeReferenceNumber, accessCode, internalId, dateTime, status, balance)
}
