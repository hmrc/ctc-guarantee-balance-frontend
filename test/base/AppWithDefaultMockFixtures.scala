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

import controllers.actions._
import models.UserAnswers
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.{GuiceFakeApplicationFactory, GuiceOneAppPerSuite}
import play.api.Application
import play.api.i18n.MessagesApi
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Call
import play.api.test.Helpers
import play.twirl.api.Html
import repositories.{MongoLockRepository, SessionRepository}
import services.{AuditService, GuaranteeBalanceService}
import uk.gov.hmrc.nunjucks.NunjucksRenderer

import scala.concurrent.Future

trait AppWithDefaultMockFixtures extends BeforeAndAfterEach with GuiceOneAppPerSuite with GuiceFakeApplicationFactory with MockitoSugar {
  self: TestSuite =>

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockRenderer,
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
  val mockSessionRepository: SessionRepository             = mock[SessionRepository]
  val mockMongoLockRepository: MongoLockRepository         = mock[MongoLockRepository]
  val mockGuaranteeBalanceService: GuaranteeBalanceService = mock[GuaranteeBalanceService]
  val mockAuditService: AuditService                       = mock[AuditService]

  protected val onwardRoute: Call        = Call("GET", "/foo")
  protected val fakeNavigator: Navigator = new FakeNavigator(onwardRoute)

  final override def fakeApplication(): Application =
    applicationBuilder()
      .build()

  protected def setExistingUserAnswers(userAnswers: UserAnswers): Unit         = setExistingUserAnswers(Some(userAnswers))
  protected def setExistingUserAnswers(userAnswers: Option[UserAnswers]): Unit = setUserAnswers(userAnswers)

  protected def setNoExistingUserAnswers(): Unit = setUserAnswers(None)

  private def setUserAnswers(userAnswers: Option[UserAnswers]): Unit =
    when(mockSessionRepository.get(any())).thenReturn(Future.successful(userAnswers))

  protected def applicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[NunjucksRenderer].toInstance(mockRenderer),
        bind[MessagesApi].toInstance(Helpers.stubMessagesApi()),
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[MongoLockRepository].toInstance(mockMongoLockRepository),
        bind[GuaranteeBalanceService].toInstance(mockGuaranteeBalanceService),
        bind[AuditService].toInstance(mockAuditService),
        bind[Navigator].toInstance(fakeNavigator)
      )
}
