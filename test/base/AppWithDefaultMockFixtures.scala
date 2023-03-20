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

package base

import config.{FrontendAppConfig, V1Module, V2Module}
import controllers.actions._
import models.UserAnswers
import navigation.{FakeNavigatorV1, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerSuite}
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.api.test.Helpers
import repositories.SessionRepository
import services.{AuditService, GuaranteeBalanceService}
import uk.gov.hmrc.mongo.lock.MongoLockRepository

import scala.concurrent.Future

trait AppWithDefaultMockFixtures extends BeforeAndAfterEach with GuiceOneAppPerSuite with GuiceFakeApplicationFactory with MockitoSugar {
  self: TestSuite =>

  override def beforeEach(): Unit = {
    reset(mockSessionRepository)
    reset(mockMongoLockRepository)
    reset(mockGuaranteeBalanceService)
    reset(mockAuditService)

    when(mockSessionRepository.set(any()))
      .thenReturn(Future.successful(true))
  }

  val mockSessionRepository: SessionRepository             = mock[SessionRepository]
  val mockMongoLockRepository: MongoLockRepository         = mock[MongoLockRepository]
  val mockGuaranteeBalanceService: GuaranteeBalanceService = mock[GuaranteeBalanceService]
  val mockAuditService: AuditService                       = mock[AuditService]
  val mockAppConfig: FrontendAppConfig                     = mock[FrontendAppConfig]

  protected val onwardRoute: Call        = Call("GET", "/foo")
  protected val fakeNavigator: Navigator = new FakeNavigatorV1(onwardRoute)

  final override def fakeApplication(): Application =
    applicationBuilder()
      .build()

  protected def setExistingUserAnswers(userAnswers: UserAnswers): Unit         = setExistingUserAnswers(Some(userAnswers))
  protected def setExistingUserAnswers(userAnswers: Option[UserAnswers]): Unit = setUserAnswers(userAnswers)

  protected def setNoExistingUserAnswers(): Unit = setUserAnswers(None)

  private def setUserAnswers(userAnswers: Option[UserAnswers]): Unit =
    when(mockSessionRepository.get(any())).thenReturn(Future.successful(userAnswers))

  private def defaultApplicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[MessagesApi].toInstance(Helpers.stubMessagesApi()),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[MongoLockRepository].toInstance(mockMongoLockRepository),
        bind[GuaranteeBalanceService].toInstance(mockGuaranteeBalanceService),
        bind[AuditService].toInstance(mockAuditService),
        bind[Navigator].toInstance(fakeNavigator)
      )

  protected def applicationBuilder(): GuiceApplicationBuilder =
    defaultApplicationBuilder()

  protected def v1ApplicationBuilder(): GuiceApplicationBuilder =
    defaultApplicationBuilder()
      .disable[V2Module]
      .bindings(new V1Module)

  protected def v2ApplicationBuilder(): GuiceApplicationBuilder =
    defaultApplicationBuilder()
      .disable[V1Module]
      .bindings(new V2Module)
}
