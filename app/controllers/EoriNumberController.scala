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
import models.{Mode, UserAnswers}
import navigation.Navigator
import pages.{EoriNumberPage, IsNctsUserPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

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

  def onPageLoad(mode: Mode, isNctsUser: Boolean): Action[AnyContent] = (identify andThen getData).async {
    implicit request: OptionalDataRequest[AnyContent] =>
      val eoriNumber: Option[String] = request.userAnswers.flatMap(
        _.get(EoriNumberPage)
      )
      val preparedForm = eoriNumber match {
        case Some(value) => form.fill(value)
        case _           => form
      }

      val json = Json.obj(
        "form"       -> preparedForm,
        "mode"       -> mode,
        "isNctsUser" -> isNctsUser
      )

      renderer.render("eoriNumber.njk", json).map(Ok(_))
  }

  def onSubmit(mode: Mode, isNctsUser: Boolean): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            val json = Json.obj(
              "form"       -> formWithErrors,
              "mode"       -> mode,
              "isNctsUser" -> isNctsUser
            )

            renderer.render("eoriNumber.njk", json).map(BadRequest(_))

          },
          value =>
            for {
              updatedAnswers <- Future.fromTry(populateUserAnswers(getOrCreateUserAnswers, value, isNctsUser))
              _              <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(EoriNumberPage, mode, updatedAnswers))
        )
  }

  private def getOrCreateUserAnswers(implicit request: OptionalDataRequest[AnyContent]): UserAnswers =
    request.userAnswers getOrElse UserAnswers(id = request.eoriNumber)

  private def populateUserAnswers(userAnswers: UserAnswers, eoriNumber: String, isNctsUser: Boolean): Try[UserAnswers] =
    userAnswers
      .set(EoriNumberPage, eoriNumber)
      .flatMap(_.set(IsNctsUserPage, isNctsUser))
}
