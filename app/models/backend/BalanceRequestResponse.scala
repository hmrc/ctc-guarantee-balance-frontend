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

package models.backend

import cats.data.NonEmptyList
import models.backend.errors.FunctionalError
import models.formats.CommonFormats
import models.values.{BalanceId, CurrencyCode, ErrorType}
import play.api.libs.json.{Json, OFormat, Reads}

import java.text.NumberFormat
import java.util.{Currency, Locale}

sealed abstract class BalanceRequestResponse

case class BalanceRequestSuccess(
  balance: BigDecimal,
  currency: CurrencyCode
) extends BalanceRequestResponse {

  def formatForDisplay: String =
    try {
      val formatter = NumberFormat.getCurrencyInstance(Locale.UK)
      formatter.setCurrency(Currency.getInstance(currency.value))
      formatter.format(balance)
    } catch {
      case _: Exception =>
        s"${currency.value}$balance"
    }
}

case class BalanceRequestPending(balanceId: BalanceId) extends BalanceRequestResponse

case class BalanceRequestNotMatched(errorPointer: String) extends BalanceRequestResponse
object BalanceRequestUnsupportedGuaranteeType extends BalanceRequestResponse

case class BalanceRequestPendingExpired(balanceId: BalanceId) extends BalanceRequestResponse

case class BalanceRequestFunctionalError(
  errors: NonEmptyList[FunctionalError]
) extends BalanceRequestResponse {
  def containsErrorType(errorType: ErrorType): Boolean = errors.exists(_.errorType == errorType)
}

object BalanceRequestResponse extends CommonFormats {

  implicit lazy val balanceRequestSuccessFormat: OFormat[BalanceRequestSuccess] =
    Json.format[BalanceRequestSuccess]

  implicit lazy val balanceRequestFunctionalErrorFormat: OFormat[BalanceRequestFunctionalError] =
    Json.format[BalanceRequestFunctionalError]

  implicit val reads: Reads[BalanceRequestResponse] = Reads[BalanceRequestResponse](
    value =>
      balanceRequestSuccessFormat.reads(value) orElse
        balanceRequestFunctionalErrorFormat.reads(value)
  )
}
