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

package views

class NotFoundViewSpec extends SingleViewSpec("notFound.njk") {

  "must render correct heading" in {
    assertPageTitleEqualsMessage(doc, "pageNotFound.heading")
  }

  "must render correct content" in {
    assertContainsText(doc, "pageNotFound.paragraph1")

    assertContainsText(doc, "pageNotFound.paragraph2")

    assertContainsText(doc, "pageNotFound.paragraph3Start")
    assertPageHasLink(doc, "contact", "pageNotFound.contactLink", frontendAppConfig.nctsEnquiriesUrl)
  }
}
