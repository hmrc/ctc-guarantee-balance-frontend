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
import forms.EoriNumberFormProvider
import models.requests.OptionalDataRequest

import javax.inject.Inject
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.EoriNumberPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import scala.concurrent.{ExecutionContext, Future}

class EoriNumberController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigator: Navigator,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  formProvider: EoriNumberFormProvider,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request: OptionalDataRequest[AnyContent] =>
      val getEoriNumber: Option[String] = request.userAnswers.flatMap(
        userAnswers => userAnswers.get(EoriNumberPage)
      )
      val preparedForm = getEoriNumber match {
        case Some(value) => form.fill(value)
        case _           => form
      }

      val json = Json.obj(
        "form" -> preparedForm,
        "mode" -> mode
      )

      renderer.render("eoriNumber.njk", json).map(Ok(_))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val json = Json.obj(
              "form" -> formWithErrors,
              "mode" -> mode
            )

            renderer.render("eoriNumber.njk", json).map(BadRequest(_))

          },
          value =>
            for {
              uaSetup        <- getOrCreateUserAnswers(request.eoriNumber)
              updatedAnswers <- Future.fromTry(uaSetup.set(EoriNumberPage, value))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(EoriNumberPage, mode, updatedAnswers))
        )
  }

  def getOrCreateUserAnswers(eoriNumber: String): Future[UserAnswers] = {
    val initialUserAnswers = UserAnswers(id = eoriNumber)

    sessionRepository.get(id = eoriNumber) map {
      userAnswers =>
        userAnswers getOrElse initialUserAnswers
    }
  }
}
