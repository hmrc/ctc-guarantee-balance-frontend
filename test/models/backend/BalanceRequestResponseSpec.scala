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

package models.backend

import base.SpecBase
import models.values.CurrencyCode
import org.scalacheck.Arbitrary.arbitrary

import java.util.Currency

// scalastyle:off magic.number
class BalanceRequestResponseSpec extends SpecBase {

  "BalanceRequestSuccess" - {

    ".formatForDisplay" - {

      "must display currencies correctly for different currency codes" - {

        "when pounds sterling" - {

          val currency: Option[CurrencyCode] = Some(CurrencyCode("GBP"))

          "when balance of 10" in {
            val balance = BalanceRequestSuccess(10, currency)
            balance.formatForDisplay mustEqual "£10.00"
          }

          "when balance of 12.34" in {
            val balance = BalanceRequestSuccess(12.34, currency)
            balance.formatForDisplay mustEqual "£12.34"
          }

          "when balance of 1000" in {
            val balance = BalanceRequestSuccess(1000, currency)
            balance.formatForDisplay mustEqual "£1,000.00"
          }

          "when balance of 10000" in {
            val balance = BalanceRequestSuccess(10000, currency)
            balance.formatForDisplay mustEqual "£10,000.00"
          }

          "when balance of 1000000" in {
            val balance = BalanceRequestSuccess(1000000, currency)
            balance.formatForDisplay mustEqual "£1,000,000.00"
          }
        }

        "when euros" - {

          val currency = Some(CurrencyCode("EUR"))

          "when balance of 10" in {
            val balance = BalanceRequestSuccess(10, currency)
            balance.formatForDisplay mustEqual "€10.00"
          }

          "when balance of 12.34" in {
            val balance = BalanceRequestSuccess(12.34, currency)
            balance.formatForDisplay mustEqual "€12.34"
          }

          "when balance of 1000" in {
            val balance = BalanceRequestSuccess(1000, currency)
            balance.formatForDisplay mustEqual "€1,000.00"
          }

          "when balance of 10000" in {
            val balance = BalanceRequestSuccess(10000, currency)
            balance.formatForDisplay mustEqual "€10,000.00"
          }

          "when balance of 1000000" in {
            val balance = BalanceRequestSuccess(1000000, currency)
            balance.formatForDisplay mustEqual "€1,000,000.00"
          }
        }
      }

      "must prepend currency code to balance for invalid currency codes" in {

        forAll(arbitrary[String].suchThat(
                 x => !Currency.getAvailableCurrencies.contains(x)
               ),
               arbitrary[BigDecimal]
        ) {
          (invalidCurrencyCode, amount) =>
            val balance = BalanceRequestSuccess(amount, Some(CurrencyCode(invalidCurrencyCode)))
            balance.formatForDisplay mustEqual s"$invalidCurrencyCode$amount"
        }
      }
    }
  }

}
// scalastyle:off magic.number
