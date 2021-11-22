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

package itBase

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.mongo.lock.MongoLockRepository

import scala.concurrent.Future

trait ItSpecBase extends AnyFreeSpec with Matchers with ScalaFutures with IntegrationPatience with BeforeAndAfterEach with GuiceOneAppPerSuite {

  val mockMongoLockRepository: MongoLockRepository = mock[MongoLockRepository]

  override def beforeEach(): Unit = {
    reset(mockMongoLockRepository)

    when(mockMongoLockRepository.releaseLock(any(), any())).thenReturn(Future.successful(()))
  }

  implicit override lazy val app: Application = new GuiceApplicationBuilder()
    .overrides(bind[MongoLockRepository].toInstance(mockMongoLockRepository))
    .build()
}
