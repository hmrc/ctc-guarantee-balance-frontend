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

import java.util.UUID

import base.{AppWithDefaultMockFixtures, SpecBase}
import cats.data.NonEmptyList
import matchers.JsonMatchers
import models.backend.errors.FunctionalError
import models.backend._
import models.requests.DataRequest
import models.values.{BalanceId, CurrencyCode, ErrorType}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify}
import pages.GuaranteeReferenceNumberPage
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.Helpers._
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse}

import scala.concurrent.Future

class GuaranteeBalanceResponseHandlerSpec extends SpecBase with JsonMatchers with AppWithDefaultMockFixtures {

  val expectedUuid = UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4")
  val balanceId    = BalanceId(expectedUuid)

  private val grn: String  = "grn"
  val populatedUserAnswers = emptyUserAnswers.set(GuaranteeReferenceNumberPage, grn).success.value

  val noMatchResponse         = Right(BalanceRequestNotMatched)
  val unsupportedTypeResponse = Right(BalanceRequestUnsupportedGuaranteeType)
  val successResponse         = Right(BalanceRequestSuccess(BigDecimal(99.9), CurrencyCode("GBP")))
  val pendingResponse         = Right(BalanceRequestPending(balanceId))
  val tryAgainResponse        = Right(BalanceRequestPendingExpired(balanceId))
  val httpErrorResponse       = Left(HttpResponse(404, ""))
  val tooManyRequestsResponse = Left(HttpResponse(429, ""))

  val functionalError      = FunctionalError(ErrorType(1), "", None)
  val balanceErrorResponse = Right(BalanceRequestFunctionalError(NonEmptyList(functionalError, Nil)))

  implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  val mockRequest      = mock[Request[AnyContent]]
  implicit val request = DataRequest(mockRequest, "eoriNumber", populatedUserAnswers)

  private lazy val handler: GuaranteeBalanceResponseHandler = app.injector.instanceOf[GuaranteeBalanceResponseHandler]

  "GuaranteeBalanceResponseHandlerSpec" - {

    "must Redirect to the TryAgain Controller if the status is empty " in {
      val result: Future[Result] = handler.processResponse(tryAgainResponse, processPending)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.TryGuaranteeBalanceAgainController.onPageLoad().url
    }

    "must Redirect to the DetailsDontMatchController if the status is NoMatch " in {
      val result: Future[Result] = handler.processResponse(noMatchResponse, processPending)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.DetailsDontMatchController.onPageLoad().url
    }

    "must Redirect to the UnsupportedGuaranteeTypeController if the status is Unsupported Type " in {
      val result: Future[Result] = handler.processResponse(unsupportedTypeResponse, processPending)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.UnsupportedGuaranteeTypeController.onPageLoad().url
    }

    "must return back to the wait page if the status is still pending " in {
      val result: Future[Result] = handler.processResponse(pendingResponse, processPending)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.WaitOnGuaranteeBalanceController.onPageLoad(balanceId).url
    }

    "must Redirect to the Balance Confirmation Controller if the status is DataReturned " in {
      val result: Future[Result] = handler.processResponse(successResponse, processPending)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.BalanceConfirmationController.onPageLoad().url
    }

    "must Redirect show the technical difficulties page if it has a processErrorResponse " in {
      val result: Future[Result] = handler.processResponse(balanceErrorResponse, processPending)

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      status(result) mustEqual INTERNAL_SERVER_ERROR
      templateCaptor.getValue mustBe "technicalDifficulties.njk"
    }

    "must Redirect to the rate limit page if there are too many requests" in {
      val result: Future[Result] = handler.processResponse(tooManyRequestsResponse, processPending)

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual controllers.routes.RateLimitController.onPageLoad().url
    }

    "must Redirect show the technical difficulties page if it has a httpResponseError " in {
      val result: Future[Result] = handler.processResponse(httpErrorResponse, processPending)

      val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
      verify(mockRenderer, times(1)).render(templateCaptor.capture(), any())(any())

      status(result) mustEqual INTERNAL_SERVER_ERROR
      templateCaptor.getValue mustBe "technicalDifficulties.njk"
    }
  }

  private def processPending(balanceId: BalanceId): Future[Result] =
    Future.successful(Redirect(controllers.routes.WaitOnGuaranteeBalanceController.onPageLoad(balanceId)))
}
