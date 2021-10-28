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
import javax.inject.Inject

import scala.concurrent.{ExecutionContext, Future}
import models.backend.{BalanceRequestResponse, PostBalanceRequestSuccessResponse, PostResponse}
import models.requests.BalanceRequest
import models.values.BalanceId
import play.api.http.{ContentTypes, HeaderNames, Status}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse, UpstreamErrorResponse}
import uk.gov.hmrc.http.HttpReads.Implicits._

class GuaranteeBalanceConnector @Inject() (http: HttpClient, appConfig: FrontendAppConfig)(implicit
  ec: ExecutionContext
) {

  def submitBalanceRequest(request: BalanceRequest)(implicit hc: HeaderCarrier): Future[BalanceRequestResponse] = {
    val url = s"${appConfig.guaranteeBalanceUrl}/balances"

    val headers = Seq(
      HeaderNames.ACCEPT -> "application/vnd.hmrc.1.0+json"
    )

    implicit val eitherBalanceIdOrResponseReads: HttpReads[BalanceRequestResponse] =
      HttpReads[HttpResponse].map {
        response =>
          response.status match {
//          case Status.ACCEPTED =>
//            Left(response.json.as[BalanceId])
            case Status.OK =>
              response.json.as[PostBalanceRequestSuccessResponse].response
          }
      }

    http.POST[BalanceRequest, BalanceRequestResponse](
      url.toString,
      request,
      headers
    )
  }

}
