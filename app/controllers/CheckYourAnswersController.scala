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
import controllers.actions._
import models.{CheckMode, UserAnswers}
import pages.GuaranteeReferenceNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
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
  config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val answers = createSections(request.userAnswers)
      val json = Json.obj(
        "section" -> Json.toJson(answers)
      )

      renderer.render("checkYourAnswers.njk", json).map(Ok(_))
  }

  //TODO onSubmit to be completed once the backend is implemented
  //TODO call .removeSpaces() on GRN before sending to backend

  def onSubmit(): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      val eoriNumber: String = request.eoriNumber

      val guaranteedReferenceNumber: Option[String] = request.userAnswers
        .flatMap(
          userAnswers => userAnswers.get(GuaranteeReferenceNumberPage)
        )

      guaranteedReferenceNumber match {
        case None => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
        case Some(guaranteedReferenceNumber: String) =>
          checkRateLimit(eoriNumber, guaranteedReferenceNumber).flatMap {
            lockTaken =>
              if (lockTaken) {
                ??? //todo this needs to be completed once the back end direction has been confirmed
              } else {
                Future.successful(Redirect(routes.RateLimitController.onPageLoad()))
              }
          }
      }
  }

  private def checkRateLimit(eoriNumber: String, guaranteedReferenceNumber: String) = {
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

}
