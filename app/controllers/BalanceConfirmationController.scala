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

package controllers

import config.FrontendAppConfig
import controllers.actions._
import pages.BalancePage
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{DateTimeService, ReferralService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.ViewProvider

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class BalanceConfirmationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  val controllerComponents: MessagesControllerComponents,
  appConfig: FrontendAppConfig,
  referralService: ReferralService,
  view: ViewProvider,
  dateTimeService: DateTimeService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad: Action[AnyContent] = actions.requireData.andThen(getMandatoryPage(BalancePage)) {
    implicit request =>
      Ok(
        view.balanceConfirmationView(
          balance = request.arg,
          timestamp = dateTimeService.timestamp,
          referral = referralService.getReferralFromSession
        )
      )
  }

  def checkAnotherGuaranteeBalance: Action[AnyContent] =
    clearUserAnswersAndRedirect(routes.StartController.startAgain().url)

  def manageTransitMovements: Action[AnyContent] =
    clearUserAnswersAndRedirect(appConfig.manageTransitMovementsUrl)

  private def clearUserAnswersAndRedirect(url: String): Action[AnyContent] = actions.requireData.async {
    implicit request =>
      sessionRepository.set(request.userAnswers.clear) map {
        _ =>
          Redirect(url)
      }
  }
}
