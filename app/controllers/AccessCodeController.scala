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
import forms.AccessCodeFormProvider
import models.Mode
import navigation.Navigator
import pages.AccessCodePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.AccessCodeView
import views.html.AccessCodeViewV2

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AccessCodeController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  actions: Actions,
  formProvider: AccessCodeFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: AccessCodeView,
  viewV2: AccessCodeViewV2,
  config: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = actions.requireData {
    implicit request =>
      val preparedForm = request.userAnswers.get(AccessCodePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      if (config.guaranteeBalanceApiV2) {
        Ok(viewV2(preparedForm, mode))
      } else {
        Ok(view(preparedForm, mode))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = actions.requireData.async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            if (config.guaranteeBalanceApiV2) {
              Future.successful(BadRequest(viewV2(formWithErrors, mode)))
            } else {
              Future.successful(BadRequest(view(formWithErrors, mode)))
            },
          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(AccessCodePage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AccessCodePage, mode, updatedAnswers))
        )
  }
}
