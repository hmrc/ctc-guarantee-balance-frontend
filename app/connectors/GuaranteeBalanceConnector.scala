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

package connectors

import cats.data.NonEmptyList
import config.FrontendAppConfig
import models.backend.*
import models.backend.errors.FunctionalError
import models.requests.BalanceRequest
import models.values.BalanceId
import models.values.ErrorType.{InvalidDataErrorType, NotMatchedErrorType}
import play.api.Logging
import play.api.http.{HeaderNames, Status}
import play.api.libs.json.Json
import play.api.libs.ws.JsonBodyWritables.*
import uk.gov.hmrc.http.HttpReads.Implicits.*
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpErrorFunctions, HttpReads, HttpResponse, StringContextOps}

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GuaranteeBalanceConnector @Inject() (http: HttpClientV2, appConfig: FrontendAppConfig)(implicit
  ec: ExecutionContext
) extends HttpErrorFunctions
    with Logging {

  private val headers = Seq(
    HeaderNames.ACCEPT -> "application/vnd.hmrc.2.0+json"
  )

  // scalastyle:off cyclomatic.complexity
  def submitBalanceRequest(request: BalanceRequest, grn: String)(implicit
    hc: HeaderCarrier
  ): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    val url = url"${appConfig.guaranteeBalanceUrl}/$grn/balance"

    implicit val eitherBalanceIdOrResponseReads: HttpReads[Either[HttpResponse, BalanceRequestResponse]] =
      HttpReads[HttpResponse].map {
        response =>
          response.status match {
            case Status.OK =>
              Right(response.json.as[BalanceRequestResponse])
            case Status.TOO_MANY_REQUESTS =>
              logger.warn("[GuaranteeBalanceConnector][submitBalanceRequest] TOO_MANY_REQUESTS response from back end call")
              Right(BalanceRequestRateLimit)
            case Status.BAD_REQUEST =>
              logger.warn(s"[GuaranteeBalanceConnector][submitBalanceRequest] BAD_REQUEST response: ${response.body}")
              response.json.as[BadRequestResponse] match {
                case BadRequestResponse("INVALID_GUARANTEE_TYPE", _) =>
                  Right(BalanceRequestUnsupportedGuaranteeType)
                case BadRequestResponse(_, message) if message.toLowerCase.contains("the guarantee reference number is not in the correct format") =>
                  Right(BalanceRequestNotMatched(response.body))
                case _ =>
                  Left(response)
              }
            case Status.NOT_FOUND =>
              logger.info(s"[GuaranteeBalanceConnector][submitBalanceRequest] NOT_FOUND response: ${response.body}")
              Right(BalanceRequestNotMatched(response.body))
            case _ =>
              logger.warn(s"[GuaranteeBalanceConnector][submitBalanceRequest] INTERNAL_SERVER_ERROR response: ${response.body}")
              Left(response)
          }
      }

    http
      .post(url)
      .setHeader(headers*)
      .withBody(Json.toJson(request))
      .execute[Either[HttpResponse, BalanceRequestResponse]]
  }
  // scalastyle:on cyclomatic.complexity

  def queryPendingBalance(balanceId: BalanceId)(implicit hc: HeaderCarrier): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    val url = url"${appConfig.guaranteeBalanceUrl}/balances/${balanceId.value}"

    implicit val eitherBalanceIdOrPendingResponseReads: HttpReads[Either[HttpResponse, BalanceRequestResponse]] =
      HttpReads[HttpResponse].map {
        response =>
          response.status match {
            case Status.OK        => Right(processQuerySuccessResponse(balanceId, response))
            case Status.NOT_FOUND => Right(BalanceRequestPendingExpired(balanceId))
            case _                => Left(response)
          }
      }

    http
      .get(url)
      .setHeader(headers*)
      .execute[Either[HttpResponse, BalanceRequestResponse]]
  }

  private def processQuerySuccessResponse(balanceId: BalanceId, response: HttpResponse): BalanceRequestResponse = {
    val balanceRequestResponse = response.json.as[GetBalanceRequestResponse].request
    balanceRequestResponse.response match {
      case Some(response) =>
        response match {
          case fe: BalanceRequestFunctionalError => convertErrorTypeToBalanceRequestResponse(fe.errors).getOrElse(response)
          case _                                 => response
        }
      case _ =>
        val currentTime        = Instant.now()
        val timeRequestExpires = balanceRequestResponse.requestedAt.plusSeconds(appConfig.guaranteeBalanceExpiryTime)
        if (currentTime.isBefore(timeRequestExpires)) BalanceRequestPending(balanceId) else BalanceRequestPendingExpired(balanceId)
    }
  }

  private def convertErrorTypeToBalanceRequestResponse(errorTypes: NonEmptyList[FunctionalError]): Option[BalanceRequestResponse] =
    errorTypes.toList.flatMap(getProcessableErrorResponses).headOption

  private def getProcessableErrorResponses(errorType: FunctionalError): Option[BalanceRequestResponse] =
    errorType match {
      case FunctionalError(NotMatchedErrorType, errorPointer, _) =>
        Some(BalanceRequestNotMatched(errorPointer))
      case FunctionalError(InvalidDataErrorType, "GRR(1).GQY(1).Query identifier", Some("R261")) =>
        Some(BalanceRequestUnsupportedGuaranteeType)
      case _ => None
    }
}
