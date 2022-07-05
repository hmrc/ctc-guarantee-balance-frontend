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
import models.{Referral, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import play.api.http.Status.SEE_OTHER
import play.api.test.FakeRequest
import play.api.test.Helpers._

class IndexControllerSpec extends SpecBase with AppWithDefaultMockFixtures {

  "must redirect to start of journey with no set referral" in {

    forAll(arbitrary[Option[UserAnswers]]) {
      userAnswers =>
        setExistingUserAnswers(userAnswers)
        val request = FakeRequest(GET, routes.IndexController.onPageLoad(Some(Referral.GovUK)).url)
        val result  = route(app, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.StartController.start(Some(Referral.GovUK)).url
    }
  }

}
