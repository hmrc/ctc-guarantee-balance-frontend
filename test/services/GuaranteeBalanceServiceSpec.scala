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

import java.util.UUID

import akka.actor.ActorSystem
import base.{AppWithDefaultMockFixtures, SpecBase}
import connectors.GuaranteeBalanceConnector
import models.backend.BalanceRequestSuccess
import models.values.{BalanceId, CurrencyCode}
import org.mockito.Matchers.{any, eq => eqTo}
import org.mockito.Mockito.when
import play.api.inject.bind

import scala.concurrent.Future

class GuaranteeBalanceServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  val expectedUuid = UUID.fromString("22b9899e-24ee-48e6-a189-97d1f45391c4")
  val balanceId    = BalanceId(expectedUuid)

  val successResponse = Right(BalanceRequestSuccess(BigDecimal(99.9), CurrencyCode("GBP")))

  "return the relevant mocked response from getGuaranteeBalance" in {
    val mockConnector = mock[GuaranteeBalanceConnector]
    when(mockConnector.pollBalanceRequest(any())).thenReturn(Future.successful(successResponse))

    val service = app.injector.instanceOf[GuaranteeBalanceService]

    service.getGuaranteeBalance(balanceId) mustEqual Future.successful(successResponse)
  }
}
