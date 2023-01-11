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

import akka.actor.ActorSystem
import akka.pattern.after
import config.FrontendAppConfig
import connectors.GuaranteeBalanceConnector
import models.backend.{BalanceRequestPending, BalanceRequestRateLimit, BalanceRequestResponse, BalanceRequestSessionExpired}
import models.requests.{BalanceRequest, BalanceRequestV2, DataRequest}
import models.values._
import pages.{AccessCodePage, BalanceIdPage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.Logging
import repositories.MongoLockRepository
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}

import javax.inject.Inject
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class GuaranteeBalanceService @Inject() (
  actorSystem: ActorSystem,
  connector: GuaranteeBalanceConnector,
  mongoLockRepository: MongoLockRepository,
  config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends Logging {

  // TODO - remove polling functionality and call to submitBalanceRequest (v1) post P5 deploy
  def retrieveBalanceResponse()(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Either[HttpResponse, BalanceRequestResponse]] =
    (config.guaranteeBalanceApiV2, request.userAnswers.get(BalanceIdPage)) match {
      case (false, Some(balanceId: BalanceId)) => pollForGuaranteeBalance(balanceId)
      case (false, None)                       => submitBalanceRequest()
      case _                                   => submitBalanceRequestV2()
    }

  // TODO - remove post P5 deploy
  private def submitBalanceRequest()(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    logger.info("[GuaranteeBalanceService][submitBalanceRequest] submit balance request")
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
  }

  private def submitBalanceRequestV2()(implicit hc: HeaderCarrier, request: DataRequest[_]): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    logger.info("[GuaranteeBalanceService][submitBalanceRequestV2] submit balance request")
    (for {
      guaranteeReferenceNumber <- request.userAnswers.get(GuaranteeReferenceNumberPage)
      accessCode               <- request.userAnswers.get(AccessCodePage)
    } yield checkRateLimit(request.internalId, guaranteeReferenceNumber).flatMap {
      lockFree =>
        if (lockFree) {
          connector
            .submitBalanceRequestV2(
              BalanceRequestV2(
                AccessCode(accessCode)
              ),
              guaranteeReferenceNumber
            )
        } else {
          logger.warn("[GuaranteeBalanceService][submit][V2] Rate Limit hit")
          Future.successful(Right(BalanceRequestRateLimit))
        }
    }).getOrElse {
      logger.warn("[GuaranteeBalanceService][submit][V2] Insufficient data in user answers.")
      Future.successful(Right(BalanceRequestSessionExpired))
    }
  }

  private def checkRateLimit(internalId: String, guaranteeReferenceNumber: String): Future[Boolean] = {
    val lockId   = LockId(internalId, guaranteeReferenceNumber).toString
    val duration = config.rateLimitDuration.seconds
    mongoLockRepository.takeLock(lockId, internalId, duration)
  }

  // TODO  - remove post P5 deploy
  private def pollForGuaranteeBalance(balanceId: BalanceId)(implicit
    hc: HeaderCarrier
  ): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    logger.info("[GuaranteeBalanceService][pollForGuaranteeBalance] poll for response")
    retryGuaranteeBalance(balanceId, System.nanoTime())
  }

  // TODO  - remove post P5 deploy
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

  // TODO  - remove post P5 deploy
  private def remainingProcessingTime(startTimeMillis: Long, maxTime: FiniteDuration): Boolean = {
    val currentTimeMillis: Long = System.nanoTime()
    val durationInSeconds       = (currentTimeMillis - startTimeMillis) / 1e9d
    durationInSeconds < maxTime.toSeconds
  }
}
