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
import models.NormalMode
import pages._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import services.AuditService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.audit.{RateLimitAuditModel, UnsuccessfulBalanceAuditModel}

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class RateLimitController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  auditService: AuditService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      val json = Json.obj("nextPageUrl" -> controllers.routes.EoriNumberController.onPageLoad(NormalMode).url)

      println("\n\n\n\n req.ua" + request.userAnswers.toString)
      auditService.audit(
        RateLimitAuditModel.build(
          request.userAnswers.get.get(EoriNumberPage).getOrElse("-").toString,
          request.userAnswers.get.get(GuaranteeReferenceNumberPage).getOrElse("-").toString,
          request.userAnswers.get.get(AccessCodePage).getOrElse("-").toString,
          SEE_OTHER,
          "Rate Limit"
        )
      )

      renderer.render("rateLimit.njk", json).map(Ok(_))
  }
}
