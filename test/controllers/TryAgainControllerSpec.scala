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

import base.{AppWithDefaultMockFixtures, SpecBase}
import models.UserAnswers
import models.backend.BalanceRequestSuccess
import models.values.{BalanceId, CurrencyCode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import pages.BalanceIdPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse}
import views.html.TryAgainView

import java.util.UUID
import scala.concurrent.Future

class TryAgainControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  private val expectedUuid: UUID   = arbitrary[UUID].sample.value
  private val balanceId: BalanceId = BalanceId(expectedUuid)

  private val baseAnswers: UserAnswers = emptyUserAnswers.setValue(BalanceIdPage, balanceId)

  private val successResponse = Right(BalanceRequestSuccess(BigDecimal(99.9), Some(CurrencyCode("GBP"))))
  private val errorResponse   = Left(HttpResponse(404: Int, ""))

  implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  "TryAgainController" - {

    "onLoad" - {
      "must return OK and the correct view for a GET" - {
        "when balance ID exists in user answers" in {
          setExistingUserAnswers(baseAnswers)

          val request = FakeRequest(GET, routes.TryAgainController.onPageLoad().url)
          val view    = injector.instanceOf[TryAgainView]
          val result  = route(app, request).value

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(Some(balanceId.value))(request, messages).toString
        }

        "when balance ID doesn't exist in user answers" in {
          setExistingUserAnswers(emptyUserAnswers)

          val request = FakeRequest(GET, routes.TryAgainController.onPageLoad().url)
          val view    = injector.instanceOf[TryAgainView]
          val result  = route(app, request).value

          status(result) mustEqual OK

          contentAsString(result) mustEqual
            view(None)(request, messages).toString
        }
      }
    }

    "onSubmit" - {
      "when balance retrieval successful" - {
        "must redirect to the balance confirmation controller" in {
          when(mockGuaranteeBalanceService.retrieveBalanceResponse()(any(), any())).thenReturn(Future.successful(successResponse))
          setExistingUserAnswers(baseAnswers)

          val request = FakeRequest(POST, routes.TryAgainController.onSubmit().url)
          val result  = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url

          verify(mockGuaranteeBalanceService, times(1)).retrieveBalanceResponse()(any(), any())
        }
      }

      "when balance retrieval unsuccessful" - {
        "must show the technical difficulties page" in {
          when(mockGuaranteeBalanceService.retrieveBalanceResponse()(any(), any())).thenReturn(Future.successful(errorResponse))
          setExistingUserAnswers(baseAnswers)

          val request = FakeRequest(POST, routes.TryAgainController.onSubmit().url)
          val result  = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url

          verify(mockGuaranteeBalanceService, times(1)).retrieveBalanceResponse()(any(), any())
        }
      }
    }
  }
}
