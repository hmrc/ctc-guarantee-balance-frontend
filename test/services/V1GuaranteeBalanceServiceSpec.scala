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
import models.requests.{BalanceRequest, DataRequest}
import models.values._
import org.apache.pekko.actor.ActorSystem
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{Assertion, BeforeAndAfterEach}
import pages.{AccessCodePage, BalanceIdPage, EoriNumberPage, GuaranteeReferenceNumberPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.{Authorization, HeaderCarrier, HttpResponse}
import uk.gov.hmrc.mongo.lock.Lock

import java.time.Instant
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future

class V1GuaranteeBalanceServiceSpec extends SpecBase with AppWithDefaultMockFixtures with BeforeAndAfterEach {

  private val expectedUuid: UUID   = UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4")
  private val balanceId: BalanceId = BalanceId(expectedUuid)

  private val successResponse               = Right(BalanceRequestSuccess(BigDecimal(99.9), Some(CurrencyCode("GBP"))))
  private val pendingResponse               = Right(BalanceRequestPending(balanceId))
  private val tryAgainResponse              = Right(BalanceRequestPendingExpired(balanceId))
  private val mockGuaranteeBalanceConnector = mock[GuaranteeBalanceConnector]

  private val actorSystem: ActorSystem = injector.instanceOf[ActorSystem]

  implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  private val grn: String    = "grn"
  private val access: String = "access"
  private val taxId: String  = "taxId"
  private val balance        = BalanceRequestSuccess(8500: Int, Some(CurrencyCode("GBP")))

  private val lock = Lock("id", "owner", Instant.now(), Instant.now())

  private val baseAnswers: UserAnswers = emptyUserAnswers
    .setValue(GuaranteeReferenceNumberPage, grn)
    .setValue(AccessCodePage, access)
    .setValue(EoriNumberPage, taxId)

  private val baseAnswersWithBalanceId: UserAnswers = emptyUserAnswers
    .setValue(GuaranteeReferenceNumberPage, grn)
    .setValue(AccessCodePage, access)
    .setValue(EoriNumberPage, taxId)
    .setValue(BalanceIdPage, balanceId)

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

        when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(None))
        when(mockGuaranteeBalanceConnector.submitBalanceRequest(any())(any()))
          .thenReturn(Future.successful(Right(balance)))

        val service = new V1GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector, mockMongoLockRepository, frontendAppConfig)
        val result  = service.retrieveBalanceResponse().futureValue
        result.value mustEqual balance

        verify(mockMongoLockRepository).takeLock(any(), any(), any())
        verify(mockGuaranteeBalanceConnector).submitBalanceRequest(eqTo(BalanceRequest(TaxIdentifier(taxId), GuaranteeReference(grn), AccessCode(access))))(
          any()
        )
      }

      "must redirect to Session Expired if no existing data is found" in {
        val userAnswers                          = emptyUserAnswers
        val request                              = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        implicit val dataRequest: DataRequest[_] = DataRequest(request, userAnswers.id, userAnswers)

        val service = new V1GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector, mockMongoLockRepository, frontendAppConfig)
        val result  = service.retrieveBalanceResponse().futureValue
        result.value mustEqual BalanceRequestSessionExpired
      }

      "must redirect to rate limit if lock in mongo repository for that user and GRN" in {
        val userAnswers                          = baseAnswers
        val request                              = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
        implicit val dataRequest: DataRequest[_] = DataRequest(request, userAnswers.id, userAnswers)

        val expectedLockId = (userAnswers.id + grn.trim.toLowerCase).hashCode.toString

        when(mockMongoLockRepository.takeLock(eqTo(expectedLockId), eqTo(userAnswers.id), any())).thenReturn(Future.successful(Some(lock)))
        val service = new V1GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector, mockMongoLockRepository, frontendAppConfig)
        val result  = service.retrieveBalanceResponse().futureValue
        result.value mustEqual BalanceRequestRateLimit

        verify(mockMongoLockRepository).takeLock(eqTo(expectedLockId), eqTo(userAnswers.id), any())
      }

      "must redirect to session timeout if at least one of EORI, GRN and access code are undefined" in {

        forAll(arbitrary[(Option[String], Option[String], Option[String])].retryUntil {
          case (eoriNumber, grn, accessCode) => !(eoriNumber.isDefined && grn.isDefined && accessCode.isDefined)
        }) {
          case (eoriNumber, grn, accessCode) =>
            val userAnswers = emptyUserAnswers
              .setValue(EoriNumberPage, eoriNumber)
              .setValue(GuaranteeReferenceNumberPage, grn)
              .setValue(AccessCodePage, accessCode)
            val request                              = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
            implicit val dataRequest: DataRequest[_] = DataRequest(request, "id", userAnswers)

            when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(None))

            when(mockGuaranteeBalanceConnector.submitBalanceRequest(any())(any()))
              .thenReturn(Future.successful(Right(balance)))

            val service = new V1GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector, mockMongoLockRepository, frontendAppConfig)
            val result  = service.retrieveBalanceResponse().futureValue
            result.value mustEqual BalanceRequestSessionExpired
        }
      }

    }

    "pollForGuaranteeBalance" - {
      val request                              = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit().url)
      implicit val dataRequest: DataRequest[_] = DataRequest(request, baseAnswersWithBalanceId.id, baseAnswersWithBalanceId)

      "return successResponse first time with a single call when we have a balanceId in the UserAnswers" in {
        val mockGuaranteeBalanceConnector = mock[GuaranteeBalanceConnector]
        when(mockGuaranteeBalanceConnector.queryPendingBalance(any())(any())).thenReturn(Future.successful(successResponse))

        val service = new V1GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector, mockMongoLockRepository, frontendAppConfig)

        val result = service.retrieveBalanceResponse()
        whenReady[Either[HttpResponse, BalanceRequestResponse], Assertion](result) {
          _ mustEqual successResponse
        }

        verify(mockGuaranteeBalanceConnector, times(1)).queryPendingBalance(any())(any())
      }

      "return successResponse first time with a single call when we Dont have a balanceId in the UserAnswers" in {
        val mockGuaranteeBalanceConnector = mock[GuaranteeBalanceConnector]
        when(mockGuaranteeBalanceConnector.queryPendingBalance(any())(any())).thenReturn(Future.successful(successResponse))

        val service = new V1GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector, mockMongoLockRepository, frontendAppConfig)

        val result = service.retrieveBalanceResponse()
        whenReady[Either[HttpResponse, BalanceRequestResponse], Assertion](result) {
          _ mustEqual successResponse
        }

        verify(mockGuaranteeBalanceConnector, times(1)).queryPendingBalance(any())(any())
      }

      "return tryAgainResponse first time with a single call" in {
        val mockGuaranteeBalanceConnector = mock[GuaranteeBalanceConnector]
        when(mockGuaranteeBalanceConnector.queryPendingBalance(any())(any())).thenReturn(Future.successful(tryAgainResponse))

        val service = new V1GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector, mockMongoLockRepository, frontendAppConfig)

        val result = service.retrieveBalanceResponse()
        whenReady[Either[HttpResponse, BalanceRequestResponse], Assertion](result) {
          _ mustEqual tryAgainResponse
        }

        verify(mockGuaranteeBalanceConnector, times(1)).queryPendingBalance(any())(any())
      }

      "first return a PendingResponse then a successResponse" in {
        val mockGuaranteeBalanceConnector = mock[GuaranteeBalanceConnector]
        when(mockGuaranteeBalanceConnector.queryPendingBalance(any())(any()))
          .thenReturn(Future.successful(pendingResponse))
          .thenReturn(Future.successful(successResponse))

        val service = new V1GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector, mockMongoLockRepository, frontendAppConfig)

        val result = service.retrieveBalanceResponse()
        whenReady[Either[HttpResponse, BalanceRequestResponse], Assertion](result) {
          _ mustEqual successResponse
        }

        verify(mockGuaranteeBalanceConnector, times(2)).queryPendingBalance(any())(any())
      }

      "return PendingResponse twice then a tryAgainResponse" in {
        val mockGuaranteeBalanceConnector = mock[GuaranteeBalanceConnector]
        when(mockGuaranteeBalanceConnector.queryPendingBalance(any())(any()))
          .thenReturn(Future.successful(pendingResponse))
          .thenReturn(Future.successful(pendingResponse))
          .thenReturn(Future.successful(tryAgainResponse))

        val service = new V1GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector, mockMongoLockRepository, frontendAppConfig)

        val result = service.retrieveBalanceResponse()

        whenReady[Either[HttpResponse, BalanceRequestResponse], Assertion](result) {
          _ mustEqual tryAgainResponse
        }

        verify(mockGuaranteeBalanceConnector, times(3)).queryPendingBalance(any())(any())
      }

      "keep returning pending until we time out, then return that status" in {
        when(mockGuaranteeBalanceConnector.queryPendingBalance(any())(any()))
          .thenReturn(Future.successful(pendingResponse))

        val service = new V1GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector, mockMongoLockRepository, frontendAppConfig)

        val result = service.retrieveBalanceResponse()
        whenReady[Either[HttpResponse, BalanceRequestResponse], Assertion](result) {
          _ mustEqual pendingResponse
        }
        // With test.application.conf waitTimeInSeconds = 1 and maxTimeInSeconds = 3
        verify(mockGuaranteeBalanceConnector, times(4: Int)).queryPendingBalance(any())(any())
      }
    }
  }
}
