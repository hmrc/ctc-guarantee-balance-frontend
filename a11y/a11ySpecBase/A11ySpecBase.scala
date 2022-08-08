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

package a11ySpecBase

import generators.Generators
import org.scalatest.OptionValues
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.scalatestaccessibilitylinter.AccessibilityMatchers

import scala.collection.immutable

trait A11ySpecBase extends AnyWordSpec with Matchers with GuiceOneAppPerSuite with AccessibilityMatchers with OptionValues with Generators {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure()
      .build()

  implicit lazy val fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/foo")

  implicit lazy val messages: Messages = {
    val messagesApi: MessagesApi = app.injector.instanceOf[MessagesApi]
    messagesApi.preferred(fakeRequest)
  }

  def heading(text: String): Html = Html(s"""<h1>$text</h1>""")

  implicit class RichHtmlFormat(html: HtmlFormat.Appendable) {

    def withHeading(text: String): Html = HtmlFormat.fill(
      immutable.Seq(
        heading(text),
        Html(html.toString())
      )
    )
  }
}
