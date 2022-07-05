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

import controllers.actions.Actions
import handlers.GuaranteeBalanceResponseHandler
import pages.BalanceIdPage
import play.api.i18n.I18nSupport
import play.api.mvc._
import services.GuaranteeBalanceService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.TryAgainView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class TryAgainController @Inject() (
  balanceService: GuaranteeBalanceService,
  val controllerComponents: MessagesControllerComponents,
  responseHandler: GuaranteeBalanceResponseHandler,
  actions: Actions,
  view: TryAgainView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = actions.requireData {
    implicit request =>
      Ok(view(request.userAnswers.get(BalanceIdPage).map(_.value)))
  }

  def onSubmit(): Action[AnyContent] = actions.requireData.async {
    implicit request =>
      balanceService.retrieveBalanceResponse.flatMap(responseHandler.processResponse(_))
  }
}
