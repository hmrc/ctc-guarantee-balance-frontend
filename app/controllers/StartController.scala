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

import controllers.actions._
import models.{NormalMode, Referral, UserAnswers}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class StartController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  updateSession: ReferralActionProvider,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def start(referral: Option[Referral]): Action[AnyContent] = (updateSession(referral) andThen actions.getData).async {
    implicit request =>
      sessionRepository.set(UserAnswers(id = request.internalId)) map {
        _ => Redirect(routes.EoriNumberController.onPageLoad(NormalMode))
      }
  }

  def startAgain(): Action[AnyContent] = actions.requireData {
    _ => Redirect(routes.EoriNumberController.onPageLoad(NormalMode))
  }
}
