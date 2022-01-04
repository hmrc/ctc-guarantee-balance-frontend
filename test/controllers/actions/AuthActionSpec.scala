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

package controllers.actions

import base.{AppWithDefaultMockFixtures, SpecBase}
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.Referral
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import play.api.mvc._
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}

import java.net.URLEncoder
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase with AppWithDefaultMockFixtures {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  class Harness(authAction: IdentifierAction) {

    def test(): Action[AnyContent] = authAction {
      _ => Results.Ok
    }
  }

  "Auth Action" - {

    "when the user has logged in" - {

      "must return OK when internal id is available" in {

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some("internalId")))

        val bodyParsers       = app.injector.instanceOf[BodyParsers.Default]
        val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

        val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, bodyParsers)

        val harness = new Harness(authAction)
        val result  = harness.test()(fakeRequest)

        status(result) mustBe OK
      }

      "must return exception when internalId is unavailable " in {

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(None))

        val bodyParsers       = app.injector.instanceOf[BodyParsers.Default]
        val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]

        val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, bodyParsers)

        val harness = new Harness(authAction)
        val result  = harness.test()(fakeRequest)

        whenReady(result.failed) {
          result =>
            result mustBe an[UnauthorizedException]
        }
      }
    }

    "when the user hasn't logged in" - {

      "must redirect the user to log in" - {

        "when user has referral cookie" in {

          forAll(arbitrary[Referral]) {
            referral =>
              val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

              val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), frontendAppConfig, bodyParsers)
              val harness    = new Harness(authAction)
              val cookie     = Cookie(Referral.cookieName, referral.toString)
              val result     = harness.test()(fakeRequest.withCookies(cookie))

              status(result) mustBe SEE_OTHER

              redirectLocation(result).get mustEqual
                s"${frontendAppConfig.loginUrl}?continue=${URLEncoder.encode(s"${frontendAppConfig.loginContinueUrl}?referral=$referral", "utf-8")}"
          }
        }

        "when user does not have referral cookie" in {

          val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

          val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), frontendAppConfig, bodyParsers)
          val harness    = new Harness(authAction)
          val result     = harness.test()(fakeRequest)

          status(result) mustBe SEE_OTHER

          redirectLocation(result).get mustEqual
            s"${frontendAppConfig.loginUrl}?continue=${URLEncoder.encode(frontendAppConfig.loginContinueUrl, "utf-8")}"
        }
      }
    }

    "when the user's session has expired" - {

      "must redirect the user to log in " in {

        val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

        val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new BearerTokenExpired), frontendAppConfig, bodyParsers)
        val harness    = new Harness(authAction)
        val result     = harness.test()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "when the user doesn't have sufficient confidence level" - {

      "must redirect the user to the unauthorised page" in {

        val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

        val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new InsufficientConfidenceLevel), frontendAppConfig, bodyParsers)
        val harness    = new Harness(authAction)
        val result     = harness.test()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "when the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in {

        val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

        val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAuthProvider), frontendAppConfig, bodyParsers)
        val harness    = new Harness(authAction)
        val result     = harness.test()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "when the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in {

        val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

        val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedAffinityGroup), frontendAppConfig, bodyParsers)
        val harness    = new Harness(authAction)
        val result     = harness.test()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "when the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in {

        val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

        val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new UnsupportedCredentialRole), frontendAppConfig, bodyParsers)
        val harness    = new Harness(authAction)
        val result     = harness.test()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }
  }
}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}
