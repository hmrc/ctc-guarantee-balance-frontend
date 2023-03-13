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
import controllers.actions.{DataRequiredAction, DataRequiredActionImpl, FakeIdentifierAction, IdentifierAction}
import forms.GuaranteeReferenceNumberFormProvider
import models.NormalMode
import navigation.Navigator
import org.mockito.Mockito.when
import pages.GuaranteeReferenceNumberPage
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.{AuditService, GuaranteeBalanceService}
import uk.gov.hmrc.mongo.lock.MongoLockRepository
import views.html.{GuaranteeReferenceNumberView, GuaranteeReferenceNumberViewV2}

class GuaranteeReferenceNumberControllerV2Spec extends SpecBase with AppWithDefaultMockFixtures {

  override protected def applicationBuilder(): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[SessionRepository].toInstance(mockSessionRepository),
        bind[MongoLockRepository].toInstance(mockMongoLockRepository),
        bind[GuaranteeBalanceService].toInstance(mockGuaranteeBalanceService),
        bind[AuditService].toInstance(mockAuditService),
        bind[Navigator].toInstance(fakeNavigator)
      )
      .configure("guaranteeBalanceApi.version" -> "2.0")

  private val formProvider                       = new GuaranteeReferenceNumberFormProvider(mockAppConfig)
  private val form                               = formProvider()
  private val mode                               = NormalMode
  private lazy val guaranteeReferenceNumberRoute = routes.GuaranteeReferenceNumberController.onPageLoad(mode).url

  private val validAnswer: String = "guaranteeRef12345"

  "GuaranteeReferenceNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      setExistingUserAnswers(emptyUserAnswers)

      when(mockAppConfig.guaranteeBalanceApiV2).thenReturn(true)

      val request = FakeRequest(GET, guaranteeReferenceNumberRoute)
      val view    = injector.instanceOf[GuaranteeReferenceNumberViewV2]
      val result  = route(app, request).value

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view(form, mode)(request, messages).toString
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.setValue(GuaranteeReferenceNumberPage, validAnswer)
      setExistingUserAnswers(userAnswers)

      val request = FakeRequest(GET, guaranteeReferenceNumberRoute)
      val view    = injector.instanceOf[GuaranteeReferenceNumberViewV2]
      val result  = route(app, request).value

      status(result) mustEqual OK

      val filledForm = form.bind(Map("value" -> validAnswer))

      contentAsString(result) mustEqual
        view(filledForm, mode)(request, messages).toString
    }

    "must redirect to the next page when valid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, guaranteeReferenceNumberRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual onwardRoute.url
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      setExistingUserAnswers(emptyUserAnswers)

      val request = FakeRequest(POST, guaranteeReferenceNumberRoute).withFormUrlEncodedBody(("value", ""))
      val view    = injector.instanceOf[GuaranteeReferenceNumberViewV2]
      val result  = route(app, request).value

      status(result) mustEqual BAD_REQUEST

      val boundForm = form.bind(Map("value" -> ""))

      contentAsString(result) mustEqual
        view(boundForm, mode)(request, messages).toString
    }

    "must redirect to Session Expired for a GET if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(GET, guaranteeReferenceNumberRoute)

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }

    "must redirect to Session Expired for a POST if no existing data is found" in {

      setNoExistingUserAnswers()

      val request = FakeRequest(POST, guaranteeReferenceNumberRoute)
        .withFormUrlEncodedBody(("value", validAnswer))

      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER

      redirectLocation(result).value mustEqual routes.SessionExpiredController.onPageLoad().url
    }
  }
}
