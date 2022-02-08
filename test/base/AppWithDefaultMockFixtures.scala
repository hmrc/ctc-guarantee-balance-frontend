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

package base

import config.FrontendAppConfig
import controllers.actions._
import models.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerSuite}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.{bind, Injector}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.{FakeRequest, Helpers}
import play.twirl.api.Html
import repositories.SessionRepository
import services.{AuditService, GuaranteeBalanceService}
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import uk.gov.hmrc.nunjucks.NunjucksRenderer

import scala.concurrent.Future

trait AppWithDefaultMockFixtures extends BeforeAndAfterEach with GuiceOneAppPerSuite with GuiceFakeApplicationFactory with MockitoSugar {
  self: TestSuite =>

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockRenderer,
      mockDataRetrievalAction,
      mockSessionRepository,
      mockMongoLockRepository,
      mockGuaranteeBalanceService,
      mockAuditService
    )

    when(mockRenderer.render(any(), any())(any()))
      .thenReturn(Future.successful(Html("")))

    when(mockSessionRepository.set(any()))
      .thenReturn(Future.successful(true))
  }

  val mockRenderer: NunjucksRenderer                       = mock[NunjucksRenderer]
  val mockDataRetrievalAction: DataRetrievalAction         = mock[DataRetrievalAction]
  val mockSessionRepository: SessionRepository             = mock[SessionRepository]
  val mockMongoLockRepository: MongoLockRepository         = mock[MongoLockRepository]
  val mockGuaranteeBalanceService: GuaranteeBalanceService = mock[GuaranteeBalanceService]
  val mockAuditService: AuditService                       = mock[AuditService]

  final override def fakeApplication(): Application =
    applicationBuilder()
      .build()

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers)),
        bind[NunjucksRenderer].toInstance(mockRenderer),
        bind[MessagesApi].toInstance(Helpers.stubMessagesApi()),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[MongoLockRepository].toInstance(mockMongoLockRepository),
        bind[GuaranteeBalanceService].toInstance(mockGuaranteeBalanceService),
        bind[AuditService].toInstance(mockAuditService)
      )

  def injector: Injector = app.injector

  def frontendAppConfig: FrontendAppConfig = injector.instanceOf[FrontendAppConfig]

  def messagesApi: MessagesApi = injector.instanceOf[MessagesApi]

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  implicit def messages: Messages = messagesApi.preferred(fakeRequest)
}
