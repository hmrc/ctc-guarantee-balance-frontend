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
    super.beforeEach()

    reset(mockMongoLockRepository)

    when(mockMongoLockRepository.releaseLock(any(), any())).thenReturn(Future.successful(()))
    when(mockMongoLockRepository.takeLock(any(), any(), any())).thenReturn(Future.successful(true))
  }

  override def fakeApplication(): Application = new GuiceApplicationBuilder()
    .overrides(bind[MongoLockRepository].toInstance(mockMongoLockRepository))
    .build()

}
