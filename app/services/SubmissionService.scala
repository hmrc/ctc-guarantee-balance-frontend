package services

import config.FrontendAppConfig
import controllers.routes
import handlers.GuaranteeBalanceResponseHandler
import models.UserAnswers
import models.requests.{BalanceRequest, DataRequest}
import models.values._
import org.joda.time.LocalDateTime
import pages.{AccessCodePage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.Logging
import play.api.http.Status.TOO_MANY_REQUESTS
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import viewModels.audit.AuditConstants.{AUDIT_DEST_RATE_LIMITED, AUDIT_ERROR_RATE_LIMIT_EXCEEDED, AUDIT_TYPE_GUARANTEE_BALANCE_RATE_LIMIT}
import viewModels.audit.{ErrorMessage, UnsuccessfulBalanceAuditModel}

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class SubmissionService @Inject()(
                                   guaranteeBalanceService: GuaranteeBalanceService,
                                   responseHandler: GuaranteeBalanceResponseHandler,
                                   auditService: AuditService,
                                   mongoLockRepository: MongoLockRepository,
                                   config: FrontendAppConfig

                                 ) (implicit ec: ExecutionContext, hc: HeaderCarrier, dr: DataRequest[_]) extends Logging {

  def submit(userAnswers: UserAnswers, internalId: String) = {
    (for {
      guaranteeReferenceNumber <- userAnswers.get(GuaranteeReferenceNumberPage)
      taxIdentifier <- userAnswers.get(EoriNumberPage)
      accessCode <- userAnswers.get(AccessCodePage)
    } yield checkRateLimit(internalId, guaranteeReferenceNumber).flatMap {
      lockFree =>
        if (lockFree) {
          guaranteeBalanceService
            .submitBalanceRequest(
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
      logger.warn("[CheckYourAnswersController][onSubmit] Insufficient data in user answers.")
      Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
    }
  }

  private def checkRateLimit(eoriNumber: String, guaranteeReferenceNumber: String): Future[Boolean] = {
    val lockId = LockId(eoriNumber, guaranteeReferenceNumber).toString
    val duration = config.rateLimitDuration.seconds
    mongoLockRepository.takeLock(lockId, eoriNumber, duration)
  }

  private def processPending(balanceId: BalanceId): Future[Result] =
    Future.successful(Redirect(controllers.routes.WaitOnGuaranteeBalanceController.onPageLoad(balanceId)))

}
