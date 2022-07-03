/*
 * Copyright 2022 HM Revenue & Customs
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
import matchers.JsonMatchers
import models.UserAnswers
import models.backend.BalanceRequestSuccess
import models.values.{BalanceId, CurrencyCode}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import pages.{AccessCodePage, BalanceIdPage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse}

import java.util.UUID
import scala.concurrent.Future

class TryAgainControllerSpec extends SpecBase with JsonMatchers with AppWithDefaultMockFixtures {

  private val expectedUuid: UUID   = UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4")
  private val balanceId: BalanceId = BalanceId(expectedUuid)

  private val grn: String    = "grn"
  private val access: String = "access"
  private val taxId: String  = "taxId"

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .setValue(GuaranteeReferenceNumberPage, grn)
    .setValue(AccessCodePage, access)
    .setValue(EoriNumberPage, taxId)

  private val baseAnswersWithBalanceId: UserAnswers = baseAnswers
    .setValue(BalanceIdPage, balanceId)

  private val successResponse = Right(BalanceRequestSuccess(BigDecimal(99.9), CurrencyCode("GBP")))
  private val errorResponse   = Left(HttpResponse(404, ""))

  implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  "TryAgainController" - {

    "onLoad" - {
      "must return OK and the correct view for a GET" in {
        val request = FakeRequest(GET, routes.TryAgainController.onPageLoad().url)

        setExistingUserAnswers(baseAnswers)
        val result = route(app, request).value

        status(result) mustEqual OK

        val templateCaptor: ArgumentCaptor[String] = ArgumentCaptor.forClass(classOf[String])
        val jsonCaptor: ArgumentCaptor[JsObject]   = ArgumentCaptor.forClass(classOf[JsObject])

        verify(mockRenderer, times(1)).render(templateCaptor.capture(), jsonCaptor.capture())(any())

        val expectedJson = Json.obj(
          "waitTimeInSeconds" -> frontendAppConfig.rateLimitDuration
        )

        jsonCaptor.getValue must containJson(expectedJson)
        templateCaptor.getValue mustBe "tryAgain.njk"
      }
    }

    "onSubmit" - {
      "must submit a request and then Redirect to the Balance Confirmation Controller if the status is DataReturned and Submit Mode" in {
        when(mockGuaranteeBalanceService.retrieveBalanceResponse()(any(), any())).thenReturn(Future.successful(successResponse))

        val request = FakeRequest(POST, routes.TryAgainController.onSubmit().url)
        setExistingUserAnswers(baseAnswersWithBalanceId)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.BalanceConfirmationController.onPageLoad().url
        verify(mockGuaranteeBalanceService, times(1)).retrieveBalanceResponse()(any(), any())
      }

      "must show the technical difficulties page if we have an error " in {
        when(mockGuaranteeBalanceService.retrieveBalanceResponse()(any(), any())).thenReturn(Future.successful(errorResponse))

        val request = FakeRequest(POST, routes.TryAgainController.onSubmit().url)

        setExistingUserAnswers(baseAnswersWithBalanceId)
        val result = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.ErrorController.technicalDifficulties().url

        verify(mockGuaranteeBalanceService, times(1)).retrieveBalanceResponse()(any(), any())
      }
    }
  }
}
