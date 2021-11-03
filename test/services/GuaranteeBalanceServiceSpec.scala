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

package services

import akka.actor.ActorSystem
import base.{AppWithDefaultMockFixtures, SpecBase}
import models.backend.{BalanceRequestPending, BalanceRequestPendingExpired, BalanceRequestSuccess}
import models.values.{BalanceId, CurrencyCode}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import java.util.UUID

import connectors.GuaranteeBalanceConnector
import org.scalatest.time.SpanSugar.convertIntToGrainOfTime

import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.Future
import scala.language.postfixOps

class GuaranteeBalanceServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  val expectedUuid = UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4")
  val balanceId    = BalanceId(expectedUuid)

  val successResponse  = Right(BalanceRequestSuccess(BigDecimal(99.9), CurrencyCode("GBP")))
  val pendingResponse  = Right(BalanceRequestPending(balanceId))
  val tryAgainResponse = Right(BalanceRequestPendingExpired(balanceId))

  val actorSystem: ActorSystem = injector.instanceOf[ActorSystem]

  implicit val hc: HeaderCarrier = HeaderCarrier(Some(Authorization("BearerToken")))

  "pollForResponse" - {
    "return successResponse first time with a single call" in {
      val mockGuaranteeBalanceConnector = mock[GuaranteeBalanceConnector]
      when(mockGuaranteeBalanceConnector.queryPendingBalance(any())(any())).thenReturn(Future.successful(successResponse))

      val service = new GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector)

      val result = service.pollForGuaranteeBalance(balanceId, 1 seconds, 5 seconds)
      whenReady(result) {
        _ mustEqual successResponse
      }

      verify(mockGuaranteeBalanceConnector, times(1)).queryPendingBalance(any())(any())
    }

    "return tryAgainResponse first time with a single call" in {
      val mockGuaranteeBalanceConnector = mock[GuaranteeBalanceConnector]
      when(mockGuaranteeBalanceConnector.queryPendingBalance(any())(any())).thenReturn(Future.successful(tryAgainResponse))

      val service = new GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector)

      val result = service.pollForGuaranteeBalance(balanceId, 1 seconds, 5 seconds)
      whenReady(result) {
        _ mustEqual tryAgainResponse
      }

      verify(mockGuaranteeBalanceConnector, times(1)).queryPendingBalance(any())(any())
    }

    "first return a PendingResponse then a successResponse" in {
      val mockGuaranteeBalanceConnector = mock[GuaranteeBalanceConnector]
      when(mockGuaranteeBalanceConnector.queryPendingBalance(any())(any()))
        .thenReturn(Future.successful(pendingResponse))
        .thenReturn(Future.successful(successResponse))

      val service = new GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector)
      val result  = service.pollForGuaranteeBalance(balanceId, 1 seconds, 5 seconds)

      whenReady(result) {
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

      val service = new GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector)

      val result = service.pollForGuaranteeBalance(balanceId, 1 seconds, 5 seconds)

      whenReady(result) {
        _ mustEqual tryAgainResponse
      }

      verify(mockGuaranteeBalanceConnector, times(3)).queryPendingBalance(any())(any())
    }

    "keep returning pending until we time out, then return that status" in {
      val mockGuaranteeBalanceConnector = mock[GuaranteeBalanceConnector]
      when(mockGuaranteeBalanceConnector.queryPendingBalance(any())(any()))
        .thenReturn(Future.successful(pendingResponse))

      val service = new GuaranteeBalanceService(actorSystem, mockGuaranteeBalanceConnector)

      val result = service.pollForGuaranteeBalance(balanceId, 3 seconds, 5 seconds)

      whenReady(result) {
        _ mustEqual pendingResponse
      }

      //First call after 0 seconds
      //Second after 3 seconds
      //Third after 6 seoncds -- Timeout here
      verify(mockGuaranteeBalanceConnector, times(3)).queryPendingBalance(any())(any())
    }
  }

}
