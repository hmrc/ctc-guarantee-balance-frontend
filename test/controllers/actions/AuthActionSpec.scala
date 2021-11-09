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

package controllers.actions

import base.{AppWithDefaultMockFixtures, SpecBase}
import com.google.inject.Inject
import controllers.actions.AuthActionSpec._
import controllers.routes
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{~, Retrieval}
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase with AppWithDefaultMockFixtures {

  val mockAuthConnector: AuthConnector = mock[AuthConnector]

  class Harness(authAction: IdentifierAction) {

    def test(expectedIdentifierRequest: Option[IdentifierRequest[AnyContent]] = None): Action[AnyContent] = authAction {
      request =>
        expectedIdentifierRequest.map {
          expected =>
            request.internalId mustEqual expected.internalId
            request.isEnrolled mustEqual expected.isEnrolled
        }
        Results.Ok
    }
  }

  def createEnrolment(key: String, identifierKey: Option[String], id: String, state: String): Enrolment =
    Enrolment(
      key = key,
      identifiers = identifierKey match {
        case Some(idKey) => Seq(EnrolmentIdentifier(idKey, id))
        case None        => Seq.empty
      },
      state = state
    )

  val emptyEnrolments: Enrolments = Enrolments(Set())

  val internalId = "internalId"

  "Auth Action" - {

    "when the user has logged in" - {

      "must return OK when internal id is available" - {

        "with isEnrolled false" - {

          "when not enrolled" in {

            when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
              .thenReturn(Future.successful(emptyEnrolments ~ Some(internalId)))

            val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

            val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, bodyParsers)

            val expectedIdentifierRequest: IdentifierRequest[AnyContent] = IdentifierRequest(fakeRequest, internalId, isEnrolled = false)

            val harness = new Harness(authAction)
            val result  = harness.test(Some(expectedIdentifierRequest))(fakeRequest)

            status(result) mustBe OK
          }

          "when incorrect identifier key" - {

            "when new enrolment" in {

              forAll(arbitrary[Option[String]].suchThat(!_.contains(frontendAppConfig.newEnrolmentIdentifierKey))) {
                identifierKey =>
                  val enrolment  = createEnrolment(frontendAppConfig.newEnrolmentKey, identifierKey, "123", "Activated")
                  val enrolments = Enrolments(Set(enrolment))

                  when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
                    .thenReturn(Future.successful(enrolments ~ Some(internalId)))

                  val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

                  val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, bodyParsers)

                  val expectedIdentifierRequest: IdentifierRequest[AnyContent] = IdentifierRequest(fakeRequest, internalId, isEnrolled = false)

                  val harness = new Harness(authAction)
                  val result  = harness.test(Some(expectedIdentifierRequest))(fakeRequest)

                  status(result) mustBe OK
              }
            }

            "when legacy enrolment" in {

              forAll(arbitrary[Option[String]].suchThat(!_.contains(frontendAppConfig.legacyEnrolmentIdentifierKey))) {
                identifierKey =>
                  val enrolment  = createEnrolment(frontendAppConfig.legacyEnrolmentKey, identifierKey, "123", "Activated")
                  val enrolments = Enrolments(Set(enrolment))

                  when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
                    .thenReturn(Future.successful(enrolments ~ Some(internalId)))

                  val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

                  val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, bodyParsers)

                  val expectedIdentifierRequest: IdentifierRequest[AnyContent] = IdentifierRequest(fakeRequest, internalId, isEnrolled = false)

                  val harness = new Harness(authAction)
                  val result  = harness.test(Some(expectedIdentifierRequest))(fakeRequest)

                  status(result) mustBe OK
              }
            }
          }

          "when enrolment is not active" - {

            "when new enrolment" in {

              forAll(arbitrary[String].suchThat(_.toLowerCase != "activated")) {
                state =>
                  val enrolment  = createEnrolment(frontendAppConfig.newEnrolmentKey, Some(frontendAppConfig.newEnrolmentIdentifierKey), "123", state)
                  val enrolments = Enrolments(Set(enrolment))

                  when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
                    .thenReturn(Future.successful(enrolments ~ Some(internalId)))

                  val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

                  val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, bodyParsers)

                  val expectedIdentifierRequest: IdentifierRequest[AnyContent] = IdentifierRequest(fakeRequest, internalId, isEnrolled = false)

                  val harness = new Harness(authAction)
                  val result  = harness.test(Some(expectedIdentifierRequest))(fakeRequest)

                  status(result) mustBe OK
              }
            }

            "when legacy enrolment" in {

              forAll(arbitrary[String].suchThat(_.toLowerCase != "activated")) {
                state =>
                  val enrolment  = createEnrolment(frontendAppConfig.legacyEnrolmentKey, Some(frontendAppConfig.legacyEnrolmentIdentifierKey), "123", state)
                  val enrolments = Enrolments(Set(enrolment))

                  when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
                    .thenReturn(Future.successful(enrolments ~ Some(internalId)))

                  val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

                  val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, bodyParsers)

                  val expectedIdentifierRequest: IdentifierRequest[AnyContent] = IdentifierRequest(fakeRequest, internalId, isEnrolled = false)

                  val harness = new Harness(authAction)
                  val result  = harness.test(Some(expectedIdentifierRequest))(fakeRequest)

                  status(result) mustBe OK
              }
            }
          }
        }

        "with isEnrolled true" - {

          "when enrolled with active enrolment" - {

            "when new enrolment" in {

              val enrolment  = createEnrolment(frontendAppConfig.newEnrolmentKey, Some(frontendAppConfig.newEnrolmentIdentifierKey), "123", "Activated")
              val enrolments = Enrolments(Set(enrolment))

              when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
                .thenReturn(Future.successful(enrolments ~ Some(internalId)))

              val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

              val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, bodyParsers)

              val expectedIdentifierRequest: IdentifierRequest[AnyContent] = IdentifierRequest(fakeRequest, internalId, isEnrolled = true)

              val harness = new Harness(authAction)
              val result  = harness.test(Some(expectedIdentifierRequest))(fakeRequest)

              status(result) mustBe OK
            }

            "when legacy enrolment" in {

              val enrolment  = createEnrolment(frontendAppConfig.legacyEnrolmentKey, Some(frontendAppConfig.legacyEnrolmentIdentifierKey), "123", "Activated")
              val enrolments = Enrolments(Set(enrolment))

              when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
                .thenReturn(Future.successful(enrolments ~ Some(internalId)))

              val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

              val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, frontendAppConfig, bodyParsers)

              val expectedIdentifierRequest: IdentifierRequest[AnyContent] = IdentifierRequest(fakeRequest, internalId, isEnrolled = true)

              val harness = new Harness(authAction)
              val result  = harness.test(Some(expectedIdentifierRequest))(fakeRequest)

              status(result) mustBe OK
            }
          }
        }
      }

      "must return exception when internalId is unavailable " in {

        when(mockAuthConnector.authorise[Enrolments ~ Option[String]](any(), any())(any(), any()))
          .thenReturn(Future.successful(emptyEnrolments ~ None))

        val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

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

      "must redirect the user to log in " in {

        val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

        val authAction = new AuthenticatedIdentifierAction(new FakeFailingAuthConnector(new MissingBearerToken), frontendAppConfig, bodyParsers)
        val harness    = new Harness(authAction)
        val result     = harness.test()(fakeRequest)

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
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

object AuthActionSpec {

  implicit class RetrievalsUtil[A](val retrieval: A) extends AnyVal {
    def `~`[B](anotherRetrieval: B): A ~ B = retrieve.~(retrieval, anotherRetrieval)
  }

}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}
