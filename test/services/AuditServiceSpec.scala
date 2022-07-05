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

package services

import base.{AppWithDefaultMockFixtures, SpecBase}
import config.FrontendAppConfig
import org.joda.time.LocalDateTime
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import play.api.test.FakeRequest
import play.api.test.Helpers.GET
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent
import viewModels.audit.SuccessfulBalanceAuditModel

import scala.concurrent.{ExecutionContext, Future}

class AuditServiceSpec extends SpecBase with AppWithDefaultMockFixtures {

  "AuditService" - {

    val mockFrontendAppConfig                                      = mock[FrontendAppConfig]
    val mockAuditConnector                                         = mock[AuditConnector]
    val auditService                                               = new AuditService(mockFrontendAppConfig, mockAuditConnector)
    implicit val hc: HeaderCarrier                                 = HeaderCarrier()
    implicit val ec: ExecutionContext                              = ExecutionContext.global
    implicit val request: FakeRequest[_]                           = FakeRequest(GET, "/check-transit-guarantee-balance/balance")
    val extendedDataEventCaptor: ArgumentCaptor[ExtendedDataEvent] = ArgumentCaptor.forClass(classOf[ExtendedDataEvent])

    "audit must call sendExtendedEvent exactly once in audit connector if there's an event" in {
      when(mockAuditConnector.sendExtendedEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

      val event = SuccessfulBalanceAuditModel.build(
        "GB1234567890",
        "123456789800",
        "1222",
        "internalId",
        LocalDateTime.now,
        200,
        "£1,000,000"
      )

      auditService.audit(event)(hc, ec, request)
      verify(mockAuditConnector, times(1)).sendExtendedEvent(extendedDataEventCaptor.capture())(any(), any())

    }
  }
}
