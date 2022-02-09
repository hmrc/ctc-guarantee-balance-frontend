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
import config.FrontendAppConfig
import connectors.GuaranteeBalanceConnector
import javax.inject.Inject
import models.backend.{BalanceRequestPending, BalanceRequestRateLimit, BalanceRequestResponse, BalanceRequestSessionExpired}
import models.requests.{BalanceRequest, DataRequest}
import models.values._
import pages.{AccessCodePage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.mongo.lock.MongoLockRepository

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class GuaranteeBalanceService @Inject() (actorSystem: ActorSystem,
                                         connector: GuaranteeBalanceConnector,
                                         mongoLockRepository: MongoLockRepository,
                                         config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends Logging {

  def submitBalanceRequest()(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Either[HttpResponse, BalanceRequestResponse]] =
    (for {
      guaranteeReferenceNumber <- request.userAnswers.get(GuaranteeReferenceNumberPage)
      taxIdentifier            <- request.userAnswers.get(EoriNumberPage)
      accessCode               <- request.userAnswers.get(AccessCodePage)
    } yield checkRateLimit(request.internalId, guaranteeReferenceNumber).flatMap {
      lockFree =>
        if (lockFree) {
          connector
            .submitBalanceRequest(
              BalanceRequest(
                TaxIdentifier(taxIdentifier),
                GuaranteeReference(guaranteeReferenceNumber),
                AccessCode(accessCode)
              )
            )
        } else {
          logger.warn("[GuaranteeBalanceService][submit] Rate Limit hit")
          Future.successful(Right(BalanceRequestRateLimit))
        }
    }).getOrElse {
      logger.warn("[GuaranteeBalanceService][submit] Insufficient data in user answers.")
      Future.successful(Right(BalanceRequestSessionExpired))
    }

  private def checkRateLimit(internalId: String, guaranteeReferenceNumber: String): Future[Boolean] = {
    val lockId   = LockId(internalId, guaranteeReferenceNumber).toString
    val duration = config.rateLimitDuration.seconds
    mongoLockRepository.takeLock(lockId, internalId, duration)
  }

  def pollForGuaranteeBalance(balanceId: BalanceId)(implicit
    hc: HeaderCarrier
  ): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    val startTimeMillis: Long = System.nanoTime()
    retryGuaranteeBalance(balanceId, startTimeMillis)
  }

  def retryGuaranteeBalance(balanceId: BalanceId, startTimeMillis: Long)(implicit
    hc: HeaderCarrier
  ): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    val delay   = config.guaranteeBalanceDelayInSecond.seconds
    val maxTime = config.guaranteeBalanceMaxTimeInSecond.seconds
    queryPendingBalance(balanceId).flatMap {
      case Right(BalanceRequestPending(_)) if remainingProcessingTime(startTimeMillis, maxTime) =>
        after(delay, actorSystem.scheduler)(retryGuaranteeBalance(balanceId, startTimeMillis))
      case result => Future.successful(result)
    }
  }

  private def queryPendingBalance(balanceId: BalanceId)(implicit hc: HeaderCarrier): Future[Either[HttpResponse, BalanceRequestResponse]] =
    connector.queryPendingBalance(balanceId)

  private def remainingProcessingTime(startTimeMillis: Long, maxTime: FiniteDuration): Boolean = {
    val currentTimeMillis: Long = System.nanoTime()
    val durationInSeconds       = (currentTimeMillis - startTimeMillis) / 1e9d
    durationInSeconds < maxTime.toSeconds
  }
}
