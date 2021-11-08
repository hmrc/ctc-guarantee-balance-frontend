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

import controllers.actions._
import models.{NormalMode, Referral, UserAnswers}
import pages.ReferralPage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Try}

class StartController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport
    with Logging {

  def start(referral: Option[Referral]): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      def storeUserAnswersAndRedirect(userAnswers: Try[UserAnswers]): Future[Result] =
        for {
          updatedAnswers <- Future.fromTry(userAnswers)
          _              <- sessionRepository.set(updatedAnswers)
        } yield Redirect(routes.EoriNumberController.onPageLoad(NormalMode))

      (request.userAnswers, referral) match {
        case (Some(userAnswers), None) =>
          logger.info("[StartController][start] User has come from within this service. Preserving session.")
          storeUserAnswersAndRedirect(Success(userAnswers))
        case (_, Some(referral)) =>
          logger.info(s"[StartController][start] User has come from $referral. Creating new user answers.")
          storeUserAnswersAndRedirect(UserAnswers(id = request.internalId).set(ReferralPage, referral))
        case (None, None) =>
          logger.info(s"[StartController][start] Cannot determine where user has come from. Redirecting to session expired.")
          Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
      }
  }
}
