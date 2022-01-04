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

package models.backend

import models.values.{BalanceId, GuaranteeReference, TaxIdentifier}
import play.api.libs.json.{Json, Reads}

import java.time.Instant

case class PendingBalanceRequest(
  balanceId: BalanceId,
  taxIdentifier: TaxIdentifier,
  guaranteeReference: GuaranteeReference,
  requestedAt: Instant,
  completedAt: Option[Instant],
  response: Option[BalanceRequestResponse]
)

object PendingBalanceRequest {

  implicit val pendingBalanceRequestFormat: Reads[PendingBalanceRequest] =
    Json.reads[PendingBalanceRequest]
}
