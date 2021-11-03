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
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import handlers.GuaranteeBalanceResponseHandler
import javax.inject.Inject
import models.values.BalanceId
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request, Result}
import renderer.Renderer
import services.GuaranteeBalanceService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class WaitOnGuaranteeBalanceController @Inject() (
  balanceService: GuaranteeBalanceService,
  val controllerComponents: MessagesControllerComponents,
  responeHandler: GuaranteeBalanceResponseHandler,
  config: FrontendAppConfig,
  identify: IdentifierAction,
  renderer: Renderer,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(balanceId: BalanceId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      displayWaitPage(balanceId)
  }

  def onSubmit(balanceId: BalanceId): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val response =
        balanceService.pollForGuaranteeBalance(balanceId, appConfig.guaranteeBalanceDelayInSecond seconds, appConfig.guaranteeBalanceMaxTimeInSecond seconds)
      response.flatMap(responeHandler.processResponse(_, displayWaitPage))
  }

  private def displayWaitPage(balanceId: BalanceId)(implicit request: Request[_]): Future[Result] = {
    val json = Json.obj(
      "balanceId"         -> balanceId,
      "waitTimeInSeconds" -> config.guaranteeBalanceDisplayDelay
    )
    renderer.render("waitOnGuaranteeBalance.njk", json).map(Ok(_))
  }

}
