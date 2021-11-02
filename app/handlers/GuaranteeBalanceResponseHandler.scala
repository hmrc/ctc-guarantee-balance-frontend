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

package handlers

import javax.inject.Inject
import models.backend.{BalanceRequestNotMatched, BalanceRequestPending, BalanceRequestResponse, BalanceRequestSuccess}
import models.requests.DataRequest
import models.values.BalanceId
import pages.BalancePage

import play.api.mvc.Results.Redirect
import play.api.mvc._

import repositories.SessionRepository
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

class GuaranteeBalanceResponseHandler @Inject() (
  sessionRepository: SessionRepository
)(implicit ec: ExecutionContext) {

  def processResponse(response: BalanceRequestResponse, processPending: BalanceId => Future[Result])(implicit
    request: DataRequest[_]
  ): Future[Result] =
    response match {
      case BalanceRequestPending(balanceId) =>
        processPending(balanceId)
      case successResponse: BalanceRequestSuccess =>
        processSuccessResponse(successResponse)
      case BalanceRequestNotMatched =>
        Future.successful(Redirect(controllers.routes.DetailsDontMatchController.onPageLoad()))
      //ToDo - Put Back in once we have this controller
      //case BalanceRequestPendingExpired(balanceId) =>
      //  Future.successful(Redirect(controllers.routes.TryAgainController.onPageLoad(balanceId)))
    }

  private def processSuccessResponse(balanceRequest: BalanceRequestSuccess)(implicit request: DataRequest[_]): Future[Result] =
    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(BalancePage, balanceRequest.formatForDisplay))
      _              <- sessionRepository.set(updatedAnswers)
    } yield Redirect(controllers.routes.BalanceConfirmationController.onPageLoad())

}
