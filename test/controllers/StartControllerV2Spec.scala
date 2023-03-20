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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import models.{NormalMode, Referral, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify
import org.scalacheck.Arbitrary.arbitrary
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global

class StartControllerV2Spec extends SpecBase with AppWithDefaultMockFixtures {

  override protected def applicationBuilder(): GuiceApplicationBuilder =
    super.v2ApplicationBuilder()

  "Start Controller" - {

    ".start" - {

      def startRoute(referral: Option[Referral]): String = routes.StartController.start(referral).url

      "must create new user answers and redirect to the next page" - {
        "when referral defined" in {

          forAll(arbitrary[Option[UserAnswers]], arbitrary[Referral]) {
            (userAnswers, referral) =>
              beforeEach()

              val time = Instant.now()
              setExistingUserAnswers(userAnswers.map(_.copy(lastUpdated = time)))
              val request = FakeRequest(GET, startRoute(Some(referral)))
              val result  = route(app, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual routes.GuaranteeReferenceNumberController.onPageLoad(NormalMode).url
              result.map(_.session(request).get(Referral.key).get mustEqual referral.toString)

              val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
              verify(mockSessionRepository).set(uaCaptor.capture)

              uaCaptor.getValue.lastUpdated.isAfter(time) mustBe true // check that new user answers have been created
          }
        }

        "when referral not defined" in {

          forAll(arbitrary[Option[UserAnswers]]) {
            userAnswers =>
              beforeEach()

              val time = Instant.now()
              setExistingUserAnswers(userAnswers.map(_.copy(lastUpdated = time)))
              val request = FakeRequest(GET, startRoute(None))
              val result  = route(app, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual routes.GuaranteeReferenceNumberController.onPageLoad(NormalMode).url
              result.map(_.session(request).get(Referral.key) mustNot be(defined))

              val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
              verify(mockSessionRepository).set(uaCaptor.capture)

              uaCaptor.getValue.lastUpdated.isAfter(time) mustBe true // check that new user answers have been created
          }
        }
      }
    }

    ".startAgain" - {

      lazy val startAgainRoute: String = routes.StartController.startAgain().url

      "when session exists" - {
        "must redirect to EORI page" in {

          setExistingUserAnswers(emptyUserAnswers)
          val request = FakeRequest(GET, startAgainRoute)
          val result  = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.GuaranteeReferenceNumberController.onPageLoad(NormalMode).url
        }
      }

      "when no session exists" - {
        "must redirect to session expired" in {

          setNoExistingUserAnswers()
          val request = FakeRequest(GET, startAgainRoute)
          val result  = route(app, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
        }
      }
    }
  }
}
