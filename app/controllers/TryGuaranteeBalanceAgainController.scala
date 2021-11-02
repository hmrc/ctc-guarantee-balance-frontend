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

import controllers.actions.{DataRetrievalAction, IdentifierAction}
import javax.inject.Inject
import models.UserAnswers
import models.requests.DataRequest
import models.values.BalanceId
import pages.GuaranteeReferenceNumberPage
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController

import scala.concurrent.{ExecutionContext, Future}

class TryGuaranteeBalanceAgainController @Inject() (
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  mongoLockRepository: MongoLockRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(balanceId: BalanceId): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      println(s"\n\n\n\n\n ${request.toString} \n\n\n\n\n\n")
      request.userAnswers match {
        case None =>
          Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
        case Some(userAnswers: UserAnswers) =>
          userAnswers.get(GuaranteeReferenceNumberPage) match {
            case None => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
            case Some(guaranteedReferenceNumber: String) =>
              val userId = request.eoriNumber
              val lockId = (userId + guaranteedReferenceNumber.trim.toLowerCase).hashCode.toString
              mongoLockRepository.releaseLock(lockId, userId)
          }
      }

      val json = Json.obj(
        "balanceId" -> balanceId
      )
      renderer.render("tryGuaranteeBalanceAgain.njk", json).map(Ok(_))
  }

  def onSubmit(balanceId: BalanceId): Action[AnyContent] = (identify andThen getData) {
    Redirect(routes.WaitOnGuaranteeBalanceController.onPageLoad(balanceId))
  }
}
