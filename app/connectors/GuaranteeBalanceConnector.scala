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

import cats.data.NonEmptyList
import config.FrontendAppConfig
import models.RichHttpResponse
import models.backend._
import models.backend.errors.FunctionalError
import models.requests.BalanceRequest
import models.values.BalanceId
import models.values.ErrorType.{InvalidDataErrorType, NotMatchedErrorType}
import play.api.Logging
import play.api.http.{HeaderNames, Status}
import play.api.libs.json.JsResult
import services.AuditService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions, HttpReads, HttpResponse}

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class GuaranteeBalanceConnector @Inject() (http: HttpClient, appConfig: FrontendAppConfig, auditService: AuditService)(implicit
  ec: ExecutionContext
) extends HttpErrorFunctions
    with Logging {

  private val headers = Seq(
    HeaderNames.ACCEPT -> "application/vnd.hmrc.1.0+json"
  )

  def submitBalanceRequest(request: BalanceRequest)(implicit hc: HeaderCarrier): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    val url = s"${appConfig.guaranteeBalanceUrl}/balances"

    implicit val eitherBalanceIdOrResponseReads: HttpReads[Either[HttpResponse, BalanceRequestResponse]] =
      HttpReads[HttpResponse].map {
        response =>
          response.status match {
            case Status.ACCEPTED =>
              Right(BalanceRequestPending(response.json.as[PostBalanceRequestPendingResponse].balanceId))
            case Status.OK =>
              Right(response.json.as[PostBalanceRequestSuccessResponse].response)
            case Status.BAD_REQUEST =>
              processSubmitErrorResponse(response)
            case status if is4xx(status) || is5xx(status) =>
              Left(response)
          }
      }

    http.POST[BalanceRequest, Either[HttpResponse, BalanceRequestResponse]](
      url,
      request,
      headers
    )
  }

  def queryPendingBalance(balanceId: BalanceId)(implicit hc: HeaderCarrier): Future[Either[HttpResponse, BalanceRequestResponse]] = {
    val url = s"${appConfig.guaranteeBalanceUrl}/balances/${balanceId.value}"

    implicit val eitherBalanceIdOrPendingResponseReads: HttpReads[Either[HttpResponse, BalanceRequestResponse]] =
      HttpReads[HttpResponse].map {
        response =>
          response.status match {
            case Status.OK                                => Right(processQuerySuccessResponse(balanceId, response))
            case Status.NOT_FOUND                         => Right(BalanceRequestPendingExpired(balanceId))
            case status if is4xx(status) || is5xx(status) => Left(response)
          }
      }

    http.GET[Either[HttpResponse, BalanceRequestResponse]](
      url,
      Seq.empty,
      headers
    )
  }

  private def processSubmitErrorResponse(response: HttpResponse): Either[HttpResponse, BalanceRequestResponse] = {
    val json: JsResult[PostBalanceRequestFunctionalErrorResponse] = response.validateJson[PostBalanceRequestFunctionalErrorResponse]
    (for {
      fe                     <- json.asOpt
      balanceRequestResponse <- convertErrorTypeToBalanceRequestResponse(fe.response.errors)
    } yield Right(balanceRequestResponse)).getOrElse {
      val outputErrorMsg = json.fold(_.toString(), fe => s"Response contains functional error type(s) ${fe.errorTypes}")
      logger.info(s"[GuaranteeBalanceConnector][processSubmitErrorResponse] $outputErrorMsg")
      Left(response)
    }
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
