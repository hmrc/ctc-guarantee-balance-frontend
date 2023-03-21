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
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DetailsDontMatchView
import views.html.v2.DetailsDontMatchViewV2

import javax.inject.Inject

sealed trait DetailsDontMatchController {

  def onPageLoad: Action[AnyContent]

}

class DetailsDontMatchControllerV1 @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: DetailsDontMatchView
) extends DetailsDontMatchController
    with FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = actions.requireData {
    implicit request =>
      Ok(view())
  }

}

class DetailsDontMatchControllerV2 @Inject() (
  override val messagesApi: MessagesApi,
  actions: Actions,
  val controllerComponents: MessagesControllerComponents,
  view: DetailsDontMatchViewV2
) extends DetailsDontMatchController
    with FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] = actions.requireData {
    implicit request =>
      Ok(view())
  }

}
