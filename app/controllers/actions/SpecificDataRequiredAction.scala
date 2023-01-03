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

package controllers.actions

import models.UserAnswers
import models.requests._
import play.api.Logging
import play.api.libs.json.Reads
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionRefiner, Result}
import queries.Gettable

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

// scalastyle:off no.whitespace.after.left.bracket
class SpecificDataRequiredActionImpl @Inject() (implicit val ec: ExecutionContext) extends SpecificDataRequiredActionProvider {

  override def getFirst[T1](
    page: Gettable[T1]
  )(implicit rds: Reads[T1]): ActionRefiner[
    DataRequest,
    SpecificDataRequestProvider1[T1]#SpecificDataRequest
  ] = new SpecificDataRequiredAction1(page)

  override def getSecond[T1, T2](
    page: Gettable[T2]
  )(implicit rds: Reads[T2]): ActionRefiner[
    SpecificDataRequestProvider1[T1]#SpecificDataRequest,
    SpecificDataRequestProvider2[T1, T2]#SpecificDataRequest
  ] = new SpecificDataRequiredAction2(page)
}

trait SpecificDataRequiredActionProvider {

  def apply[T1](
    page: Gettable[T1]
  )(implicit rds: Reads[T1]): ActionRefiner[DataRequest, SpecificDataRequestProvider1[T1]#SpecificDataRequest] = getFirst(page)

  def getFirst[T1](
    page: Gettable[T1]
  )(implicit rds: Reads[T1]): ActionRefiner[
    DataRequest,
    SpecificDataRequestProvider1[T1]#SpecificDataRequest
  ]

  def getSecond[T1, T2](
    page: Gettable[T2]
  )(implicit rds: Reads[T2]): ActionRefiner[
    SpecificDataRequestProvider1[T1]#SpecificDataRequest,
    SpecificDataRequestProvider2[T1, T2]#SpecificDataRequest
  ]
}

trait SpecificDataRequiredAction extends Logging {

  def getPage[T, R](userAnswers: UserAnswers, page: Gettable[T])(block: T => R)(implicit rds: Reads[T]): Future[Either[Result, R]] =
    Future.successful {
      userAnswers.get(page) match {
        case Some(value) =>
          Right(block(value))
        case None =>
          logger.error(s"Mandatory answer missing from user answers at ${page.path}. Redirecting to session expired.")
          Left(Redirect(controllers.routes.SessionExpiredController.onPageLoad()))
      }
    }
}

class SpecificDataRequiredAction1[T1](
  page: Gettable[T1]
)(implicit val executionContext: ExecutionContext, rds: Reads[T1])
    extends ActionRefiner[
      DataRequest,
      SpecificDataRequestProvider1[T1]#SpecificDataRequest
    ]
    with SpecificDataRequiredAction {

  override protected def refine[A](
    request: DataRequest[A]
  ): Future[Either[Result, SpecificDataRequestProvider1[T1]#SpecificDataRequest[A]]] =
    getPage(request.userAnswers, page) {
      value =>
        new SpecificDataRequestProvider1[T1].SpecificDataRequest(
          request = request,
          internalId = request.internalId,
          userAnswers = request.userAnswers,
          arg = value
        )
    }
}

class SpecificDataRequiredAction2[T1, T2](
  page: Gettable[T2]
)(implicit val executionContext: ExecutionContext, rds: Reads[T2])
    extends ActionRefiner[
      SpecificDataRequestProvider1[T1]#SpecificDataRequest,
      SpecificDataRequestProvider2[T1, T2]#SpecificDataRequest
    ]
    with SpecificDataRequiredAction {

  override protected def refine[A](
    request: SpecificDataRequestProvider1[T1]#SpecificDataRequest[A]
  ): Future[Either[Result, SpecificDataRequestProvider2[T1, T2]#SpecificDataRequest[A]]] =
    getPage(request.userAnswers, page) {
      value =>
        new SpecificDataRequestProvider2[T1, T2].SpecificDataRequest(
          request = request,
          internalId = request.internalId,
          userAnswers = request.userAnswers,
          arg = (request.arg, value)
        )
    }
}
// scalastyle:on no.whitespace.after.left.bracket
