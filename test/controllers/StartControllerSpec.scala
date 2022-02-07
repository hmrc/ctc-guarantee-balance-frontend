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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import matchers.JsonMatchers
import models.{NormalMode, Referral, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.viewmodels.NunjucksSupport

import java.time.LocalDateTime
import scala.concurrent.ExecutionContext.Implicits.global

class StartControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers with AppWithDefaultMockFixtures {

  "Start Controller" - {

    ".start" - {

      def startRoute(referral: Option[Referral]): String = routes.StartController.start(referral).url

      "must create new user answers and redirect to the next page" - {
        "when referral defined" in {

          forAll(arbitrary[Option[UserAnswers]], arbitrary[Referral]) {
            (userAnswers, referral) =>
              beforeEach()

              val time        = LocalDateTime.now()
              val application = applicationBuilder(userAnswers = userAnswers.map(_.copy(lastUpdated = time))).build()
              val request     = FakeRequest(GET, startRoute(Some(referral)))
              val result      = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual routes.EoriNumberController.onPageLoad(NormalMode).url
              result.map(_.session(request).get(Referral.key).get mustEqual referral.toString)

              val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
              verify(mockSessionRepository).set(uaCaptor.capture)

              uaCaptor.getValue.lastUpdated.isAfter(time) mustBe true // check that new user answers have been created

              application.stop()
          }
        }

        "when referral not defined" in {

          forAll(arbitrary[Option[UserAnswers]]) {
            userAnswers =>
              beforeEach()

              val time        = LocalDateTime.now()
              val application = applicationBuilder(userAnswers = userAnswers.map(_.copy(lastUpdated = time))).build()
              val request     = FakeRequest(GET, startRoute(None))
              val result      = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual routes.EoriNumberController.onPageLoad(NormalMode).url
              result.map(_.session(request).get(Referral.key) mustNot be(defined)

              val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
              verify(mockSessionRepository).set(uaCaptor.capture)

              uaCaptor.getValue.lastUpdated.isAfter(time) mustBe true // check that new user answers have been created

              application.stop()
          }
        }
      }
    }

    ".startAgain" - {

      lazy val startAgainRoute: String = routes.StartController.startAgain().url

      "when session exists" - {
        "must redirect to EORI page" in {

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()
          val request     = FakeRequest(GET, startAgainRoute)
          val result      = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.EoriNumberController.onPageLoad(NormalMode).url

          application.stop()
        }
      }

      "when no session exists" - {
        "must redirect to session expired" in {

          val application = applicationBuilder(userAnswers = None).build()
          val request     = FakeRequest(GET, startAgainRoute)
          val result      = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url

          application.stop()
        }
      }
    }
  }
}
