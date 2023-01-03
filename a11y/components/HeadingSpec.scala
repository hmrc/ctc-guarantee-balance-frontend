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

package components

import a11ySpecBase.A11ySpecBase
import org.scalacheck.Gen
import views.html.components.Heading
import views.html.templates.MainTemplate

class HeadingSpec extends A11ySpecBase {

  "the 'heading' component" must {
    val template  = app.injector.instanceOf[MainTemplate]
    val component = app.injector.instanceOf[Heading]

    val title   = nonEmptyString.sample.value
    val heading = nonEmptyString.sample.value
    val caption = Gen.option(nonEmptyString).sample.value
    val classes = Gen.oneOf(None, Some("govuk-heading-xl govuk-!-margin-top-0 govuk-!-margin-bottom-2")).sample.value

    val content = template.apply(title) {
      component.apply(heading, caption, classes)
    }

    "pass accessibility checks" in {
      content.toString() must passAccessibilityChecks
    }
  }
}
