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

package views

import forms.GuaranteeReferenceNumberFormProvider
import models.NormalMode
import org.scalacheck.{Arbitrary, Gen}
import play.api.data.Form
import play.twirl.api.HtmlFormat
import viewModels.InputSize
import views.behaviours.InputTextViewBehaviours
import views.html.GuaranteeReferenceNumberView

class GuaranteeReferenceNumberViewSpec extends InputTextViewBehaviours[String] {

  override def form: Form[String] = new GuaranteeReferenceNumberFormProvider()()

  override def applyView(form: Form[String]): HtmlFormat.Appendable =
    injector.instanceOf[GuaranteeReferenceNumberView].apply(form, NormalMode)(fakeRequest, messages)

  override val prefix: String = "guaranteeReferenceNumber"

  implicit override val arbitraryT: Arbitrary[String] = Arbitrary(Gen.alphaNumStr)

  behave like pageWithTitle()

  behave like pageWithBackLink()

  behave like pageWithHeading()

  behave like pageWithContent("p", "You can only check the balance for:")

  behave like pageWithList(
    "govuk-list--bullet",
    "comprehensive guarantee",
    "guarantee waiver",
    "individual guarantee with multiple usage"
  )

  behave like pageWithContent("p", "You must make sure this reference is up to date.")

  behave like pageWithInputText(Some(InputSize.Width20))

  behave like pageWithContinueButton("Continue")
}
