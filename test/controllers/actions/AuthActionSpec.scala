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

package controllers.actions

import base.SpecBase
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import models.Referral
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.BeforeAndAfterEach
import play.api.mvc.*
import play.api.test.Helpers.*
import services.ReferralService
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase with BeforeAndAfterEach {

  class Harness(authAction: IdentifierAction) {

    def test(): Action[AnyContent] = authAction {
      _ => Results.Ok
    }
  }

  private val mockAuthConnector: AuthConnector = mock[AuthConnector]
  private val mockReferralService              = mock[ReferralService]
  private val mockFrontendAppConfig            = mock[FrontendAppConfig]

  override def beforeEach(): Unit = {
    super.beforeEach()

    when(mockFrontendAppConfig.loginUrl).thenReturn("http://localhost:9949/auth-login-stub/gg-sign-in")

    when(mockFrontendAppConfig.loginContinueUrl).thenReturn("http://localhost:9462/check-transit-guarantee-balance")
  }

  "Auth Action" - {

    "when the user has logged in" - {

      "must return OK when internal id is available" in {

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(Some("internalId")))

        val authAction = new AuthenticatedIdentifierAction(
          mockAuthConnector,
          mockFrontendAppConfig,
          bodyParser,
          mockReferralService
        )

        val harness = new Harness(authAction)
        val result  = harness.test()(fakeRequest)

        status(result) mustEqual OK
      }

      "must return exception when internalId is unavailable " in {

        when(mockAuthConnector.authorise[Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(None))

        val authAction = new AuthenticatedIdentifierAction(
          mockAuthConnector,
          mockFrontendAppConfig,
          bodyParser,
          mockReferralService
        )

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

        "when session has referral value" in {

          forAll(arbitrary[Referral]) {
            referral =>
              when(mockReferralService.getReferralFromSession(any()))
                .thenReturn(Some(referral))

              val authAction = new AuthenticatedIdentifierAction(
                new FakeFailingAuthConnector(new MissingBearerToken),
                mockFrontendAppConfig,
                bodyParser,
                mockReferralService
              )

              val harness = new Harness(authAction)
              val result  = harness.test()(fakeRequest.withSession(Referral.key -> referral.toString))

              status(result) mustEqual SEE_OTHER

              redirectLocation(result).get mustEqual
                s"http://localhost:9949/auth-login-stub/gg-sign-in?continue=http%3A%2F%2Flocalhost%3A9462%2Fcheck-transit-guarantee-balance%3Freferral%3D$referral"
          }
        }

        "when session does not have referral value" in {
          when(mockReferralService.getReferralFromSession(any()))
            .thenReturn(None)

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new MissingBearerToken),
            mockFrontendAppConfig,
            bodyParser,
            mockReferralService
          )

          val harness = new Harness(authAction)
          val result  = harness.test()(fakeRequest)

          status(result) mustEqual SEE_OTHER

          redirectLocation(result).get mustEqual
            s"http://localhost:9949/auth-login-stub/gg-sign-in?continue=http%3A%2F%2Flocalhost%3A9462%2Fcheck-transit-guarantee-balance"
        }
      }
    }

    "when the user's session has expired" - {

      "must redirect the user to log in " in {

        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new BearerTokenExpired),
          mockFrontendAppConfig,
          bodyParser,
          mockReferralService
        )

        val harness = new Harness(authAction)
        val result  = harness.test()(fakeRequest)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).get mustEqual
          s"http://localhost:9949/auth-login-stub/gg-sign-in?continue=http%3A%2F%2Flocalhost%3A9462%2Fcheck-transit-guarantee-balance"
      }
    }

    "when the user doesn't have sufficient confidence level" - {

      "must redirect the user to the unauthorised page" in {

        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new InsufficientConfidenceLevel),
          mockFrontendAppConfig,
          bodyParser,
          mockReferralService
        )

        val harness = new Harness(authAction)
        val result  = harness.test()(fakeRequest)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.UnauthorisedController.onPageLoad().url
      }
    }

    "when the user used an unaccepted auth provider" - {

      "must redirect the user to the unauthorised page" in {

        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new UnsupportedAuthProvider),
          mockFrontendAppConfig,
          bodyParser,
          mockReferralService
        )

        val harness = new Harness(authAction)
        val result  = harness.test()(fakeRequest)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.UnauthorisedController.onPageLoad().url
      }
    }

    "when the user has an unsupported affinity group" - {

      "must redirect the user to the unauthorised page" in {

        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
          mockFrontendAppConfig,
          bodyParser,
          mockReferralService
        )

        val harness = new Harness(authAction)
        val result  = harness.test()(fakeRequest)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.UnauthorisedController.onPageLoad().url
      }
    }

    "when the user has an unsupported credential role" - {

      "must redirect the user to the unauthorised page" in {

        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new UnsupportedCredentialRole),
          mockFrontendAppConfig,
          bodyParser,
          mockReferralService
        )

        val harness = new Harness(authAction)
        val result  = harness.test()(fakeRequest)

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.UnauthorisedController.onPageLoad().url
      }
    }
  }
}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}
