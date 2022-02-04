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
import controllers.routes
import handlers.GuaranteeBalanceResponseHandler
import models.UserAnswers
import models.backend.{BalanceRequestPending, BalanceRequestResponse}
import models.requests.{BalanceRequest, DataRequest}
import models.values._
import org.joda.time.LocalDateTime
import pages.{AccessCodePage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.Logging
import play.api.http.Status.TOO_MANY_REQUESTS
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse}
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import viewModels.audit.AuditConstants.{AUDIT_DEST_RATE_LIMITED, AUDIT_ERROR_RATE_LIMIT_EXCEEDED, AUDIT_TYPE_GUARANTEE_BALANCE_RATE_LIMIT}
import viewModels.audit.{ErrorMessage, UnsuccessfulBalanceAuditModel}

import javax.inject.Inject
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class GuaranteeBalanceService @Inject() (actorSystem: ActorSystem,
                                         connector: GuaranteeBalanceConnector,
                                         responseHandler: GuaranteeBalanceResponseHandler,
                                         auditService: AuditService,
                                         mongoLockRepository: MongoLockRepository,
                                         config: FrontendAppConfig
)(implicit ec: ExecutionContext, hc: HeaderCarrier, dr: DataRequest[_])
    extends Logging {

  def submit(userAnswers: UserAnswers, internalId: String): Future[Result] =
    (for {
      guaranteeReferenceNumber <- userAnswers.get(GuaranteeReferenceNumberPage)
      taxIdentifier            <- userAnswers.get(EoriNumberPage)
      accessCode               <- userAnswers.get(AccessCodePage)
    } yield checkRateLimit(internalId, guaranteeReferenceNumber).flatMap {
      lockFree =>
        if (lockFree) {
          submitBalanceRequest(
            BalanceRequest(
              TaxIdentifier(taxIdentifier),
              GuaranteeReference(guaranteeReferenceNumber),
              AccessCode(accessCode)
            )
          )
            .flatMap(responseHandler.processResponse(_, processPending))
        } else {
          auditService.audit(
            UnsuccessfulBalanceAuditModel.build(
              AUDIT_TYPE_GUARANTEE_BALANCE_RATE_LIMIT,
              taxIdentifier,
              guaranteeReferenceNumber,
              accessCode,
              internalId,
              LocalDateTime.now,
              TOO_MANY_REQUESTS,
              ErrorMessage(AUDIT_ERROR_RATE_LIMIT_EXCEEDED, AUDIT_DEST_RATE_LIMITED)
            )
          )
          Future.successful(Redirect(routes.RateLimitController.onPageLoad()))
        }
    }).getOrElse {
      logger.warn("[GuaranteeBalanceService][submit] Insufficient data in user answers.")
      Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
    }

  private def checkRateLimit(eoriNumber: String, guaranteeReferenceNumber: String): Future[Boolean] = {
    val lockId   = LockId(eoriNumber, guaranteeReferenceNumber).toString
    val duration = config.rateLimitDuration.seconds
    mongoLockRepository.takeLock(lockId, eoriNumber, duration)
  }

  private def processPending(balanceId: BalanceId): Future[Result] =
    Future.successful(Redirect(controllers.routes.WaitOnGuaranteeBalanceController.onPageLoad(balanceId)))

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
