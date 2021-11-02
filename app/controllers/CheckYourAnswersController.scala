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

package controllers

import config.FrontendAppConfig
import connectors.GuaranteeBalanceConnector
import controllers.actions._
import handlers.GuaranteeBalanceResponseHandler
import models.requests.BalanceRequest
import models.values.{AccessCode, BalanceId, GuaranteeReference, TaxIdentifier}
import models.{CheckMode, UserAnswers}
import pages.{AccessCodePage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import renderer.Renderer
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.CheckYourAnswersHelper
import viewModels.Section
import javax.inject.Inject

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
  guaranteeBalanceConnector: GuaranteeBalanceConnector,
  responeHandler: GuaranteeBalanceResponseHandler,
  config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val answers = createSections(request.userAnswers)
      val json = Json.obj(
        "section" -> Json.toJson(answers)
      )

      renderer.render("checkYourAnswers.njk", json).map(Ok(_))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(GuaranteeReferenceNumberPage) match {
        case None => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
        case Some(guaranteedReferenceNumber: String) =>
          checkRateLimit(request.eoriNumber, guaranteedReferenceNumber).flatMap {
            lockFree =>
              if (lockFree) {

                val taxIdentifier: String = request.userAnswers.get(EoriNumberPage).getOrElse("") //toDO confirm that this is Eori!!!
                val accessCode: String    = request.userAnswers.get(AccessCodePage).getOrElse("")

                if (taxIdentifier.isEmpty || accessCode.isEmpty) {
                  Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
                } else {

                  val response =
                    guaranteeBalanceConnector
                      .submitBalanceRequest(BalanceRequest(TaxIdentifier(taxIdentifier), GuaranteeReference(guaranteedReferenceNumber), AccessCode(accessCode)))
                  response.flatMap(responeHandler.processResponse(_, processPending))
                }

              } else {
                Future.successful(Redirect(routes.RateLimitController.onPageLoad()))
              }
          }
      }
  }

  private def checkRateLimit(eoriNumber: String, guaranteedReferenceNumber: String): Future[Boolean] = {
    val lockId   = (eoriNumber + guaranteedReferenceNumber.trim.toLowerCase).hashCode.toString
    val duration = config.rateLimitDuration.seconds
    mongoLockRepository.takeLock(lockId, eoriNumber, duration)
  }

  private def createSections(userAnswers: UserAnswers): Section = {
    val helper = new CheckYourAnswersHelper(userAnswers, CheckMode)

    Section(
      Seq(
        helper.eoriNumber,
        helper.guaranteeReferenceNumber,
        helper.accessCode
      ).flatten
    )
  }

  private def processPending(balanceId: BalanceId): Future[Result] =
    Future.successful(Redirect(controllers.routes.WaitOnGuaranteeBalanceController.onPageLoad(balanceId)))
}
