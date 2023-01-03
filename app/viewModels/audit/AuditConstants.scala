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

package viewModels.audit

object AuditConstants {
  final val AUDIT_TYPE_GUARANTEE_BALANCE_RATE_LIMIT = "GuaranteeBalanceRateLimit"
  final val AUDIT_TYPE_GUARANTEE_BALANCE_SUBMISSION = "GuaranteeBalanceSubmission"

  final val AUDIT_FIELD_EORI_NUMBER             = "eoriNumber"
  final val AUDIT_FIELD_GRN_NUMBER              = "guaranteeReferenceNumber"
  final val AUDIT_FIELD_ACCESS_CODE             = "accessCode"
  final val AUDIT_FIELD_GG_INTERNAL_ID          = "internalId"
  final val AUDIT_FIELD_DATE_TIME               = "transactionDateTime"
  final val AUDIT_FIELD_STATUS                  = "status"
  final val AUDIT_FIELD_BALANCE                 = "balance"
  final val AUDIT_FIELD_ERROR_MESSAGE           = "errorMessage"
  final val AUDIT_FIELD_DISPLAYED_ERROR_MESSAGE = "displayedErrorMessage"

  final val AUDIT_ERROR_INCORRECT_EORI        = "Incorrect EORI"
  final val AUDIT_ERROR_INCORRECT_GRN         = "Incorrect Guarantee Reference Number"
  final val AUDIT_ERROR_INCORRECT_ACCESS_CODE = "Incorrect access code"
  final val AUDIT_ERROR_EORI_GRN_DO_NOT_MATCH = "EORI and Guarantee reference number do not match"
  final val AUDIT_ERROR_DO_NOT_MATCH          = "The submitted details do not match our records"
  final val AUDIT_ERROR_RATE_LIMIT_EXCEEDED   = "Rate Limit Exceeded"
  final val AUDIT_ERROR_UNSUPPORTED_TYPE      = "Balance Request Unsupported Guarantee Type"
  final val AUDIT_ERROR_REQUEST_EXPIRED       = "Balance Request Pending Expired"

  final val AUDIT_DEST_DETAILS_DO_NOT_MATCH   = "details do not match"
  final val AUDIT_DEST_TECHNICAL_DIFFICULTIES = "technical difficulties"
  final val AUDIT_DEST_TRY_AGAIN              = "try again"
  final val AUDIT_DEST_UNSUPPORTED_TYPE       = "guarantee type not supported"
  final val AUDIT_DEST_RATE_LIMITED           = "rate limited"
}
