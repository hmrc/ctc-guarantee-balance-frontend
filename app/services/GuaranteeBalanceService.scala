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

package services

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.pattern.after
import config.FrontendAppConfig
import connectors.GuaranteeBalanceConnector
import models.backend.{BalanceRequestPending, BalanceRequestRateLimit, BalanceRequestResponse, BalanceRequestSessionExpired}
import models.requests.{BalanceRequest, BalanceRequestV2, DataRequest}
import models.values._
import pages.{AccessCodePage, BalanceIdPage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.mongo.lock.{Lock, MongoLockRepository}

import javax.inject.Inject
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

sealed trait GuaranteeBalanceService extends Logging {

  val mongoLockRepository: MongoLockRepository

  val config: FrontendAppConfig

  def retrieveBalanceResponse()(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Either[HttpResponse, BalanceRequestResponse]]

  /** @param internalId internal ID
    * @param guaranteeReferenceNumber Guarantee Reference Number
    * @return
    *  - None if there is already a lock for the given lock ID (rate limit hit)
    *  - Some(lock) if there is not already a lock for the given lock ID and a lock has been successfully created
    */
  def checkRateLimit(internalId: String, guaranteeReferenceNumber: String): Future[Option[Lock]] = {
    val lockId   = LockId(internalId, guaranteeReferenceNumber).toString
    val duration = config.rateLimitDuration.seconds
    mongoLockRepository.takeLock(lockId, internalId, duration)
  }
}

class V1GuaranteeBalanceService @Inject() (
  actorSystem: ActorSystem,
  connector: GuaranteeBalanceConnector,
  override val mongoLockRepository: MongoLockRepository,
  override val config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends GuaranteeBalanceService {

  override def retrieveBalanceResponse()(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Either[HttpResponse, BalanceRequestResponse]] =
    request.userAnswers.get(BalanceIdPage) match {
      case Some(balanceId: BalanceId) => pollForGuaranteeBalance(balanceId)
      case None                       => submitBalanceRequest()
    }

  private def submitBalanceRequest()(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    logger.info("[GuaranteeBalanceService][submitBalanceRequest] submit balance request")
    (for {
      guaranteeReferenceNumber <- request.userAnswers.get(GuaranteeReferenceNumberPage)
      taxIdentifier            <- request.userAnswers.get(EoriNumberPage)
      accessCode               <- request.userAnswers.get(AccessCodePage)
    } yield checkRateLimit(request.internalId, guaranteeReferenceNumber).flatMap {
      case Some(_) =>
        connector
          .submitBalanceRequest(
            BalanceRequest(
              TaxIdentifier(taxIdentifier),
              GuaranteeReference(guaranteeReferenceNumber),
              AccessCode(accessCode)
            )
          )
      case _ =>
        logger.warn("[GuaranteeBalanceService][submit] Rate Limit hit")
        Future.successful(Right(BalanceRequestRateLimit))
    }).getOrElse {
      logger.warn("[GuaranteeBalanceService][submit] Insufficient data in user answers.")
      Future.successful(Right(BalanceRequestSessionExpired))
    }
  }

  private def pollForGuaranteeBalance(balanceId: BalanceId)(implicit
    hc: HeaderCarrier
  ): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    logger.info("[GuaranteeBalanceService][pollForGuaranteeBalance] poll for response")
    retryGuaranteeBalance(balanceId, System.nanoTime())
  }

  private def retryGuaranteeBalance(balanceId: BalanceId, startTimeMillis: Long)(implicit
    hc: HeaderCarrier
  ): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    val delay   = config.guaranteeBalanceDelayInSecond.seconds
    val maxTime = config.guaranteeBalanceMaxTimeInSecond.seconds
    connector.queryPendingBalance(balanceId).flatMap {
      case Right(BalanceRequestPending(_)) if remainingProcessingTime(startTimeMillis, maxTime) =>
        after(delay, actorSystem.scheduler)(retryGuaranteeBalance(balanceId, startTimeMillis))
      case result => Future.successful(result)
    }
  }

  private def remainingProcessingTime(startTimeMillis: Long, maxTime: FiniteDuration): Boolean = {
    val currentTimeMillis: Long = System.nanoTime()
    val durationInSeconds       = (currentTimeMillis - startTimeMillis) / 1e9d
    durationInSeconds < maxTime.toSeconds
  }
}

class V2GuaranteeBalanceService @Inject() (
  connector: GuaranteeBalanceConnector,
  override val mongoLockRepository: MongoLockRepository,
  override val config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends GuaranteeBalanceService {

  override def retrieveBalanceResponse()(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Either[HttpResponse, BalanceRequestResponse]] =
    submitBalanceRequest()

  private def submitBalanceRequest()(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    logger.info("[GuaranteeBalanceService][submitBalanceRequestV2] submit balance request")
    (for {
      guaranteeReferenceNumber <- request.userAnswers.get(GuaranteeReferenceNumberPage)
      accessCode               <- request.userAnswers.get(AccessCodePage)
    } yield checkRateLimit(request.internalId, guaranteeReferenceNumber).flatMap {
      case Some(_) =>
        connector
          .submitBalanceRequestV2(
            BalanceRequestV2(AccessCode(accessCode)),
            guaranteeReferenceNumber
          )
      case _ =>
        logger.warn("[GuaranteeBalanceService][submit][V2] Rate Limit hit")
        Future.successful(Right(BalanceRequestRateLimit))
    }).getOrElse {
      logger.warn("[GuaranteeBalanceService][submit][V2] Insufficient data in user answers.")
      Future.successful(Right(BalanceRequestSessionExpired))
    }
  }
}
