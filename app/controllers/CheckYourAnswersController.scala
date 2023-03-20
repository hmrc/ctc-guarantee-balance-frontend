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

import controllers.actions._
import handlers.{GuaranteeBalanceResponseHandlerV1, GuaranteeBalanceResponseHandlerV2}
import play.api.Logging
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.GuaranteeBalanceService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.CheckYourAnswersViewModel.CheckYourAnswersViewModelProvider
import views.ViewProvider

import javax.inject.Inject
import scala.concurrent.ExecutionContext

sealed trait CheckYourAnswersController extends Logging {

  def onPageLoad(): Action[AnyContent]

  def onSubmit(): Action[AnyContent]

}

class CheckYourAnswersControllerV1 @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  guaranteeBalanceService: GuaranteeBalanceService,
  viewModelProvider: CheckYourAnswersViewModelProvider,
  responseHandler: GuaranteeBalanceResponseHandlerV1,
  view: ViewProvider
)(implicit ec: ExecutionContext)
    extends CheckYourAnswersController
    with FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = actions.requireData {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers)
      Ok(view.checkYourAnswers(Seq(viewModel.section)))
  }

  def onSubmit(): Action[AnyContent] = actions.requireData.async {
    implicit request =>
      guaranteeBalanceService.retrieveBalanceResponse().flatMap(responseHandler.processResponse(_))
  }
}

class CheckYourAnswersControllerV2 @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  guaranteeBalanceService: GuaranteeBalanceService,
  viewModelProvider: CheckYourAnswersViewModelProvider,
  responseHandler: GuaranteeBalanceResponseHandlerV2,
  view: ViewProvider
)(implicit ec: ExecutionContext)
    extends CheckYourAnswersController
    with FrontendBaseController
    with I18nSupport
    with Logging {

  def onPageLoad(): Action[AnyContent] = actions.requireData {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers)
      Ok(view.checkYourAnswers(Seq(viewModel.section)))
  }

  def onSubmit(): Action[AnyContent] = actions.requireData.async {
    implicit request =>
      guaranteeBalanceService.retrieveBalanceResponse().flatMap(responseHandler.processResponse(_))
  }
}
