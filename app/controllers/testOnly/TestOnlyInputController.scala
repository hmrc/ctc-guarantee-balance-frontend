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

package controllers.testOnly

import forms.testOnly.TestOnlyInputFormProvider
import models.UserAnswers
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.testOnly.TestOnlyInputView

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TestOnlyInputController @Inject() (
  override val messagesApi: MessagesApi,
  formProvider: TestOnlyInputFormProvider,
  sessionRepository: SessionRepository,
  val controllerComponents: MessagesControllerComponents,
  view: TestOnlyInputView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(): Action[AnyContent] = Action {
    implicit request =>
      Ok(view(form))
  }

  def onSubmit(): Action[AnyContent] = Action.async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors))),
          value => {
            val data        = Json.obj("testOnlyInput" -> value)
            val userAnswers = UserAnswers(id = "test-only", data = data, lastUpdated = Instant.now())
            sessionRepository.set(userAnswers).map {
              _ => Redirect(routes.TestOnlyInputController.onPageLoad())
            }
          }
        )
  }
}
