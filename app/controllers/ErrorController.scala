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

import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.{NotFoundView, TechnicalDifficultiesView}
import views.html.templates.ErrorTemplate

class ErrorController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  errorTemplate: ErrorTemplate,
  notFoundView: NotFoundView,
  technicalDifficultiesView: TechnicalDifficultiesView
) extends FrontendBaseController
    with I18nSupport {

  def badRequest: Action[AnyContent] = Action {
    implicit request =>
      BadRequest(
        errorTemplate(
          title = "global.error.badRequest400.title",
          header = "global.error.badRequest400.heading",
          message = "global.error.badRequest400.message"
        )
      )
  }

  def notFound: Action[AnyContent] = Action {
    implicit request =>
      NotFound(notFoundView())
  }

  def technicalDifficulties: Action[AnyContent] = Action {
    implicit request =>
      InternalServerError(technicalDifficultiesView())
  }

  def internalServerError: Action[AnyContent] = Action {
    implicit request =>
      InternalServerError(
        errorTemplate(
          title = "global.error.InternalServerError500.title",
          header = "global.error.InternalServerError500.heading",
          message = "global.error.InternalServerError500.message"
        )
      )
  }

}
