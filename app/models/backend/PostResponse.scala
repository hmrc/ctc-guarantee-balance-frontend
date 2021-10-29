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

import models.values.BalanceId
import play.api.libs.json.{Json, OFormat}

sealed abstract class PostResponse

case class PostBalanceRequestSuccessResponse(response: BalanceRequestSuccess) extends PostResponse

case class PostBalanceRequestPendingResponse(balanceId: BalanceId) extends PostResponse

case class PostBalanceRequestFunctionalErrorResponse(
  code: String,
  message: String,
  response: BalanceRequestFunctionalError
) extends PostResponse

case class GetBalanceRequestResponse(request: PendingBalanceRequest)

object PostResponse {

  implicit lazy val balanceRequestSuccessFormat: OFormat[PostBalanceRequestSuccessResponse] =
    Json.format[PostBalanceRequestSuccessResponse]

  implicit lazy val balanceRequestPendingFormat: OFormat[PostBalanceRequestPendingResponse] =
    Json.format[PostBalanceRequestPendingResponse]

  implicit lazy val balanceRequestFunctionalErrorFormat: OFormat[PostBalanceRequestFunctionalErrorResponse] =
    Json.format[PostBalanceRequestFunctionalErrorResponse]
}
