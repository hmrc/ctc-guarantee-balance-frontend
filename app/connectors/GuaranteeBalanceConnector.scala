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

package connectors

import config.FrontendAppConfig
import models.backend.{BalanceRequestPending, BalanceRequestResponse, PostBalanceRequestPendingResponse, PostBalanceRequestSuccessResponse}
import models.requests.BalanceRequest
import play.api.http.{HeaderNames, Status}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GuaranteeBalanceConnector @Inject() (http: HttpClient, appConfig: FrontendAppConfig)(implicit
  ec: ExecutionContext
) extends HttpErrorFunctions {

  def submitBalanceRequest(request: BalanceRequest)(implicit hc: HeaderCarrier): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    val url = s"${appConfig.guaranteeBalanceUrl}/balances"

    val headers = Seq(
      HeaderNames.ACCEPT -> "application/vnd.hmrc.1.0+json"
    )

    implicit val eitherBalanceIdOrResponseReads: HttpReads[Either[HttpResponse, BalanceRequestResponse]] =
      HttpReads[HttpResponse].map {
        response =>
          response.status match {
            case Status.ACCEPTED =>
              Right(BalanceRequestPending(response.json.as[PostBalanceRequestPendingResponse].balanceId))
            case Status.OK =>
              Right(response.json.as[PostBalanceRequestSuccessResponse].response)
            case status if is4xx(status) || is5xx(status) => Left(response)
          }
      }

    http.POST[BalanceRequest, Either[HttpResponse, BalanceRequestResponse]](
      url.toString,
      request,
      headers
    )
  }

}
