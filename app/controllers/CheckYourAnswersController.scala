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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import handlers.GuaranteeBalanceResponseHandler
import javax.inject.Inject
import models.requests.BalanceRequest
import models.values._
import org.joda.time.LocalDateTime
import pages.{AccessCodePage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import renderer.Renderer
import services.{AuditService, GuaranteeBalanceService}
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import viewModels.CheckYourAnswersViewModelProvider
import viewModels.audit.AuditConstants.{AUDIT_DEST_RATE_LIMITED, AUDIT_ERROR_RATE_LIMIT_EXCEEDED, AUDIT_TYPE_GUARANTEE_BALANCE_RATE_LIMIT}
import viewModels.audit.{ErrorMessage, UnsuccessfulBalanceAuditModel}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  mongoLockRepository: MongoLockRepository,
  guaranteeBalanceService: GuaranteeBalanceService,
  responseHandler: GuaranteeBalanceResponseHandler,
  config: FrontendAppConfig,
  viewModelProvider: CheckYourAnswersViewModelProvider,
  auditService: AuditService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers)

      val json = Json.obj(
        "section" -> Json.toJson(viewModel.section)
      )

      renderer.render("checkYourAnswers.njk", json).map(Ok(_))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      (for {
        guaranteeReferenceNumber <- request.userAnswers.get(GuaranteeReferenceNumberPage)
        taxIdentifier            <- request.userAnswers.get(EoriNumberPage)
        accessCode               <- request.userAnswers.get(AccessCodePage)
      } yield checkRateLimit(request.internalId, guaranteeReferenceNumber).flatMap {
        lockFree =>
          if (lockFree) {
            for {
              _ <- releaseLock(request.internalId, guaranteeReferenceNumber)
              balanceRequest = BalanceRequest(TaxIdentifier(taxIdentifier), GuaranteeReference(guaranteeReferenceNumber), AccessCode(accessCode))
              response <- guaranteeBalanceService.submitBalanceRequest(balanceRequest)
              result   <- responseHandler.processResponse(response, processPending)
            } yield result
          } else {
            auditService.audit(
              UnsuccessfulBalanceAuditModel.build(
                AUDIT_TYPE_GUARANTEE_BALANCE_RATE_LIMIT,
                taxIdentifier,
                guaranteeReferenceNumber,
                accessCode,
                request.internalId,
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

  private def checkRateLimit(internalId: String, guaranteeReferenceNumber: String): Future[Boolean] =
    mongoLockRepository.takeLock(lockId(internalId, guaranteeReferenceNumber), internalId, config.rateLimitDuration.seconds)

  private def releaseLock(internalId: String, guaranteeReferenceNumber: String): Future[Unit] =
    mongoLockRepository.releaseLock(lockId(internalId, guaranteeReferenceNumber), internalId)

  private def lockId(internalId: String, guaranteeReferenceNumber: String): String =
    LockId(internalId, guaranteeReferenceNumber).toString

  private def processPending(balanceId: BalanceId): Future[Result] =
    Future.successful(Redirect(controllers.routes.WaitOnGuaranteeBalanceController.onPageLoad(balanceId)))
}
