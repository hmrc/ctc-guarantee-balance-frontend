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

package services

import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.GuaranteeBalanceConnector
import controllers.routes
import models.UserAnswers
import models.backend._
import models.requests.{BalanceRequestV2, DataRequest}
import models.values._
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import pages.{AccessCodePage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class V2GuaranteeBalanceServiceSpec extends SpecBase with AppWithDefaultMockFixtures with BeforeAndAfterEach {

  private val mockGuaranteeBalanceConnector = mock[GuaranteeBalanceConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  private val grn: String    = "grn"
  private val access: String = "access"
  private val taxId: String  = "taxId"
  private val balance        = BalanceRequestSuccess(8500: Int, None)

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .setValue(GuaranteeReferenceNumberPage, grn)
    .setValue(AccessCodePage, access)
    .setValue(EoriNumberPage, taxId)

  override def beforeEach(): Unit = {
    reset(mockGuaranteeBalanceConnector)
    super.beforeEach()
  }

  "retrieveBalanceResponse" - {
    "submitBalanceRequest" - {
      "must submit the return the balance response for a successful call" in {
        val userAnswers                          = baseAnswers
        val request                              = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        implicit val dataRequest: DataRequest[_] = DataRequest(request, userAnswers.id, baseAnswers)

        when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(true))
        when(mockGuaranteeBalanceConnector.submitBalanceRequestV2(any(), any())(any()))
          .thenReturn(Future.successful(Right(balance)))

        val service = new V2GuaranteeBalanceService(mockGuaranteeBalanceConnector, mockMongoLockRepository, frontendAppConfig)
        val result  = service.retrieveBalanceResponse().futureValue
        result.value mustEqual balance

        verify(mockMongoLockRepository).takeLock(any(), any(), any())
        verify(mockGuaranteeBalanceConnector).submitBalanceRequestV2(
          eqTo(BalanceRequestV2(AccessCode(access))),
          eqTo(grn)
        )(
          any()
        )
      }

      "must redirect to Session Expired if no existing data is found" in {
        val userAnswers                          = emptyUserAnswers
        val request                              = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        implicit val dataRequest: DataRequest[_] = DataRequest(request, userAnswers.id, userAnswers)

        val service = new V2GuaranteeBalanceService(mockGuaranteeBalanceConnector, mockMongoLockRepository, frontendAppConfig)
        val result  = service.retrieveBalanceResponse().futureValue
        result.value mustEqual BalanceRequestSessionExpired
      }

      "must redirect to rate limit if lock in mongo repository for that user and GRN" in {
        val userAnswers                          = baseAnswers
        val request                              = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        implicit val dataRequest: DataRequest[_] = DataRequest(request, userAnswers.id, userAnswers)

        val expectedLockId = (userAnswers.id + grn.trim.toLowerCase).hashCode.toString

        when(mockMongoLockRepository.takeLock(eqTo(expectedLockId), eqTo(userAnswers.id), any())).thenReturn(Future.successful(false))
        val service = new V2GuaranteeBalanceService(mockGuaranteeBalanceConnector, mockMongoLockRepository, frontendAppConfig)
        val result  = service.retrieveBalanceResponse().futureValue
        result.value mustEqual BalanceRequestRateLimit

        verify(mockMongoLockRepository).takeLock(eqTo(expectedLockId), eqTo(userAnswers.id), any())
      }

      "must redirect to session timeout if at least one of GRN and access code are undefined" in {

        forAll(arbitrary[(Option[String], Option[String])].retryUntil {
          case (grn, accessCode) => !(grn.isDefined && accessCode.isDefined)
        }) {
          case (grn, accessCode) =>
            val userAnswers = emptyUserAnswers
              .setValue(GuaranteeReferenceNumberPage, grn)
              .setValue(AccessCodePage, accessCode)
            val request                              = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
            implicit val dataRequest: DataRequest[_] = DataRequest(request, "id", userAnswers)

            when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(true))

            when(mockGuaranteeBalanceConnector.submitBalanceRequestV2(any(), any())(any()))
              .thenReturn(Future.successful(Right(balance)))

            val service = new V2GuaranteeBalanceService(mockGuaranteeBalanceConnector, mockMongoLockRepository, frontendAppConfig)
            val result  = service.retrieveBalanceResponse().futureValue
            result.value mustEqual BalanceRequestSessionExpired
        }
      }
    }
  }
}
