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

package templates

import a11ySpecBase.A11ySpecBase
import org.scalacheck.Arbitrary.arbitrary
import views.html.templates.MainTemplate

class MainTemplateSpec extends A11ySpecBase {

  "the 'main' template" must {
    val template = app.injector.instanceOf[MainTemplate]

    val title          = nonEmptyString.sample.value
    val timeoutEnabled = arbitrary[Boolean].sample.value
    val canSignOut     = arbitrary[Boolean].sample.value
    val showBackLink   = arbitrary[Boolean].sample.value

    val content = template.apply(title, timeoutEnabled, canSignOut, showBackLink) {
      heading(title)
    }

    "pass accessibility checks" in {
      content.toString() must passAccessibilityChecks
    }
  }
}
