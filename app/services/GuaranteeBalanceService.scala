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

package services

import java.util.UUID

import models.values.BalanceId
import akka.actor.ActorSystem
import javax.inject.Inject

import scala.concurrent.duration._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import akka.pattern.after
import connectors.GuaranteeBalanceConnector
import models.backend.{BalanceRequestPending, BalanceRequestResponse}
import uk.gov.hmrc.http.HttpResponse

class GuaranteeBalanceService @Inject() (val actorSystem: ActorSystem, val connector: GuaranteeBalanceConnector)(implicit ec: ExecutionContext) {

  def testPoll(balanceId: BalanceId): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    val startTimeMillis: Long = System.nanoTime()
    retryGuaranteeBalance(balanceId, 2 seconds, 5 seconds, startTimeMillis)
  }

  def pollForGuaranteeBalance(balanceId: BalanceId, delay: FiniteDuration, maxTime: FiniteDuration): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    val startTimeMillis: Long = System.nanoTime()
    retryGuaranteeBalance(balanceId, delay, maxTime, startTimeMillis)
  }

  def retryGuaranteeBalance(balanceId: BalanceId,
                            delay: FiniteDuration,
                            maxTime: FiniteDuration,
                            startTimeMillis: Long
  ): Future[Either[HttpResponse, BalanceRequestResponse]] =
    getGuaranteeBalance(balanceId).flatMap {
      case Right(BalanceRequestPending(_)) if underMaxProcessingTime(startTimeMillis, maxTime) =>
        after(delay, actorSystem.scheduler)(retryGuaranteeBalance(balanceId, delay, maxTime, startTimeMillis))
      case result => Future.successful(result)
    }

  def getGuaranteeBalance(balanceId: BalanceId): Future[Either[HttpResponse, BalanceRequestResponse]] =
    connector.pollBalanceRequest(balanceId)

  def underMaxProcessingTime(startTimeMillis: Long, maxTime: FiniteDuration): Boolean = {
    val currentTimeMillis: Long = System.nanoTime()
    val durationInSeconds       = (currentTimeMillis - startTimeMillis) / 1000
    durationInSeconds < maxTime.toSeconds
  }
}
