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

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import pages.GuaranteeReferenceNumberPage
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TryGuaranteeBalanceAgainController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(GuaranteeReferenceNumberPage) match {
        case Some(guaranteeReferenceNumber: String) => renderer.render("tryGuaranteeBalanceAgain.njk").map(Ok(_))
        case None                                   => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
      }
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData) {
    Redirect(routes.BalanceConfirmationController.onPageLoad())
  }
}
