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

import config.FrontendAppConfig
import connectors.GuaranteeBalanceConnector
import models.backend.{BalanceRequestRateLimit, BalanceRequestResponse, BalanceRequestSessionExpired}
import models.requests.{BalanceRequest, DataRequest}
import models.values.*
import pages.{AccessCodePage, GuaranteeReferenceNumberPage}
import play.api.Logging
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.mongo.lock.{Lock, MongoLockRepository}

import javax.inject.Inject
import scala.concurrent.duration.*
import scala.concurrent.{ExecutionContext, Future}

class GuaranteeBalanceService @Inject() (
  connector: GuaranteeBalanceConnector,
  val mongoLockRepository: MongoLockRepository,
  val config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends Logging {

  def submitBalanceRequest()(implicit hc: HeaderCarrier, request: DataRequest[?]): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    logger.info("[GuaranteeBalanceService][submitBalanceRequest] submit balance request")
    (for {
      guaranteeReferenceNumber <- request.userAnswers.get(GuaranteeReferenceNumberPage)
      accessCode               <- request.userAnswers.get(AccessCodePage)
    } yield checkRateLimit(request.internalId, guaranteeReferenceNumber).flatMap {
      case Some(_) =>
        connector
          .submitBalanceRequest(
            BalanceRequest(AccessCode(accessCode)),
            guaranteeReferenceNumber
          )
      case _ =>
        logger.warn("[GuaranteeBalanceService][submit] Rate Limit hit")
        Future.successful(Right(BalanceRequestRateLimit))
    }).getOrElse {
      logger.warn("[GuaranteeBalanceService][submit] Insufficient data in user answers.")
      Future.successful(Right(BalanceRequestSessionExpired))
    }
  }

  /** @param internalId
    *   internal ID
    * @param guaranteeReferenceNumber
    *   Guarantee Reference Number
    * @return
    *   - None if there is already a lock for the given lock ID (rate limit hit)
    *   - Some(lock) if there is not already a lock for the given lock ID and a lock has been successfully created
    */
  private def checkRateLimit(internalId: String, guaranteeReferenceNumber: String): Future[Option[Lock]] = {
    val lockId   = LockId(internalId, guaranteeReferenceNumber).toString
    val duration = config.rateLimitDuration.seconds
    mongoLockRepository.takeLock(lockId, internalId, duration)
  }
}
