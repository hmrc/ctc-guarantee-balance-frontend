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
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import javax.inject.Inject
import models.BalanceId
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.{BalanceStatus, GuaranteeBalanceService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

class WaitOnGuaranteeBalanceController @Inject() (cc: MessagesControllerComponents,
                                                  renderer: Renderer,
                                                  balanceService: GuaranteeBalanceService,
                                                  config: FrontendAppConfig,
                                                  identify: IdentifierAction,
                                                  getData: DataRetrievalAction
)(implicit
  ec: ExecutionContext
) extends FrontendController(cc)
    with I18nSupport {

  def onPageLoad(balanceId: BalanceId): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      val json = Json.obj(
        "balanceId"         -> balanceId,
        "waitTimeInSeconds" -> config.waitTimeInSeconds
      )
      renderer.render("waitOnGuaranteeBalance.njk", json).map(Ok(_))
  }

  def onSubmit(balanceId: BalanceId): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      balanceService.getGuaranteeBalance(balanceId).flatMap {
        case Some(BalanceStatus.PendingStatus) =>
          renderer.render("waitOnGuaranteeBalance.njk").map(Ok(_))
        case Some(BalanceStatus.DataReturned) =>
          renderer.render("controlDecision.njk").map(Ok(_))
        case Some(BalanceStatus.NoMatch) =>
          Future.successful(Redirect(routes.DetailsDontMatchController.onPageLoad()))
        case None =>
          Future.successful(Redirect(routes.TryGuaranteeBalanceAgainController.onPageLoad(balanceId)))
      }
  }

}
