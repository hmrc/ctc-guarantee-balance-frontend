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

package controllers

import base.{AppWithDefaultMockFixtures, SpecBase}
import matchers.JsonMatchers
import models.{NormalMode, Referral, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.verify
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.{AccessCodePage, EoriNumberPage, GuaranteeReferenceNumberPage, ReferralPage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.viewmodels.NunjucksSupport

import java.time.LocalDateTime

class StartControllerSpec extends SpecBase with MockitoSugar with NunjucksSupport with JsonMatchers with AppWithDefaultMockFixtures {

  def startRoute(referral: Referral): String = routes.StartController.start(referral).url
  def startRoute(): String                   = routes.StartController.start().url

  "Start Controller" - {

    // format: off
    "must redirect to the next page and preserve referral when there are existing user answers" in {

      forAll(arbitrary[Referral]) {
        referral =>
          beforeEach()

          val userAnswers = emptyUserAnswers
            .set(EoriNumberPage, "eori").success.value
            .set(GuaranteeReferenceNumberPage, "grn").success.value
            .set(AccessCodePage, "code").success.value
            .set(ReferralPage, referral).success.value

          val time = LocalDateTime.now()

          val application =
            applicationBuilder(userAnswers = Some(userAnswers.copy(lastUpdated = time)))
              .build()

          val request =
            FakeRequest(GET, startRoute())

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.EoriNumberController.onPageLoad(NormalMode).url

          val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(uaCaptor.capture)

          uaCaptor.getValue.lastUpdated mustBe time // check that user answers have been persisted
          uaCaptor.getValue.get(EoriNumberPage) mustNot be(defined)
          uaCaptor.getValue.get(GuaranteeReferenceNumberPage) mustNot be(defined)
          uaCaptor.getValue.get(AccessCodePage) mustNot be(defined)
          uaCaptor.getValue.get(ReferralPage).get mustBe referral

          application.stop()
      }
    }
    // format: on

    "must redirect to the next page and set referral when there are no existing user answers" in {

      forAll(arbitrary[Referral]) {
        referral =>
          beforeEach()

          val time = LocalDateTime.now()

          val application =
            applicationBuilder(userAnswers = None)
              .build()

          val request =
            FakeRequest(GET, startRoute(referral))

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.EoriNumberController.onPageLoad(NormalMode).url

          val uaCaptor: ArgumentCaptor[UserAnswers] = ArgumentCaptor.forClass(classOf[UserAnswers])
          verify(mockSessionRepository).set(uaCaptor.capture)

          uaCaptor.getValue.lastUpdated.isAfter(time) mustBe true // check that new user answers have been created
          uaCaptor.getValue.get(ReferralPage).get mustBe referral

          application.stop()
      }
    }

  }
}
