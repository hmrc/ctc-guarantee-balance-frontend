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

package services

import akka.actor.ActorSystem
import akka.pattern.after
import connectors.GuaranteeBalanceConnector
import models.backend.{BalanceRequestPending, BalanceRequestResponse}
import models.values.BalanceId
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import javax.inject.Inject
import models.requests.BalanceRequest

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class GuaranteeBalanceService @Inject() (val actorSystem: ActorSystem, val connector: GuaranteeBalanceConnector)(implicit ec: ExecutionContext) {

  def submitBalanceRequest(balanceRequest: BalanceRequest)(implicit hc: HeaderCarrier): Future[Either[HttpResponse, BalanceRequestResponse]] =
    connector.submitBalanceRequest(balanceRequest)

  def pollForGuaranteeBalance(balanceId: BalanceId, delay: FiniteDuration, maxTime: FiniteDuration)(implicit
    hc: HeaderCarrier
  ): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    val startTimeMillis: Long = System.nanoTime()
    retryGuaranteeBalance(balanceId, delay, maxTime, startTimeMillis)
  }

  def retryGuaranteeBalance(balanceId: BalanceId, delay: FiniteDuration, maxTime: FiniteDuration, startTimeMillis: Long)(implicit
    hc: HeaderCarrier
  ): Future[Either[HttpResponse, BalanceRequestResponse]] =
    queryPendingBalance(balanceId).flatMap {
      case Right(BalanceRequestPending(_)) if remainingProcessingTime(startTimeMillis, maxTime) =>
        after(delay, actorSystem.scheduler)(retryGuaranteeBalance(balanceId, delay, maxTime, startTimeMillis))
      case result => Future.successful(result)
    }

  private def queryPendingBalance(balanceId: BalanceId)(implicit hc: HeaderCarrier): Future[Either[HttpResponse, BalanceRequestResponse]] =
    connector.queryPendingBalance(balanceId)

  private def remainingProcessingTime(startTimeMillis: Long, maxTime: FiniteDuration): Boolean = {
    val currentTimeMillis: Long = System.nanoTime()
    val durationInSeconds       = (currentTimeMillis - startTimeMillis) / 1e9d
    durationInSeconds < maxTime.toSeconds
  }
}
