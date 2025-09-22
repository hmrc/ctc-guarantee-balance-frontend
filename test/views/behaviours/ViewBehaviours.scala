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

package views.behaviours

import base.{AppWithDefaultMockFixtures, SpecBase}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.twirl.api.HtmlFormat
import play.twirl.api.TwirlHelperImports.*
import views.viewBase.ViewSpecAssertions

trait ViewBehaviours extends SpecBase with AppWithDefaultMockFixtures with ViewSpecAssertions {

  private val path = "foo"

  override def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", path)

  def view: HtmlFormat.Appendable

  def parseView(view: HtmlFormat.Appendable): Document = Jsoup.parse(view.toString())

  lazy val doc: Document = parseView(view)

  val prefix: String

  "must render service name link in header" in {
    val link = getElementByClass(doc, "govuk-header__service-name")
    assertElementContainsText(link, "Manage your transit movements")
    assertElementContainsHref(link, "/check-transit-guarantee-balance/start-again")
  }

  "must render accessibility statement link" in {
    val link = doc
      .select(".govuk-footer__inline-list-item > .govuk-footer__link")
      .toList
      .find(_.text() == "Accessibility statement")
      .get

    getElementHref(link) mustEqual s"http://localhost:12346/accessibility-statement/check-transit-guarantee-balance?referrerUrl=$path"
  }

  "must not render language toggle" in {
    assertElementDoesNotExist(doc, "hmrc-language-select")
  }

  "must render 'page not working properly' link" in {
    val link = getElementByClass(doc, "hmrc-report-technical-issue")

    assertElementContainsText(link, "Is this page not working properly? (opens in new tab)")
    getElementHref(link) mustEqual s"http://localhost:9250/contact/report-technical-problem?service=CTCTraders&referrerUrl=$path"
  }

  def pageWithTitle(args: Any*): Unit =
    pageWithTitle(doc, prefix, args*)

  def pageWithTitle(doc: Document, prefix: String, args: Any*): Unit =
    "must render title" in {
      val title      = doc.title()
      val messageKey = s"$prefix.title"
      title mustEqual s"${messages(messageKey, args*)} - Guarantee balance - Manage your transit movements - GOV.UK"
      assert(messages.isDefinedAt(messageKey))
    }

  def pageWithHeading(args: Any*): Unit =
    pageWithHeading(doc, prefix, args*)

  def pageWithHeading(doc: Document, prefix: String, args: Any*): Unit =
    "must render heading" in {
      val heading    = getElementByTag(doc, "h1")
      val messageKey = s"$prefix.heading"
      assertElementIncludesText(heading, messages(messageKey, args*))
      assert(messages.isDefinedAt(messageKey))
    }

  def pageWithCaption(expectedText: String): Unit =
    "must render caption" in {
      val caption = getElementByClass(doc, "govuk-caption-xl")
      assertElementContainsText(caption, expectedText)
    }

  def pageWithHint(expectedText: String): Unit =
    "must render hint" in {
      val hint = getElementByClass(doc, "govuk-hint")
      assertElementContainsText(hint, expectedText)
    }

  def pageWithoutHint(): Unit =
    "must not render hint" in {
      assertElementDoesNotExist(doc, "govuk-hint")
    }

  def pageWithSubmitButton(expectedText: String): Unit =
    pageWithButton("submit", expectedText)

  def pageWithContinueButton(expectedText: String): Unit =
    pageWithButton("continue", expectedText)

  private def pageWithButton(id: String, expectedText: String): Unit =
    pageWithButton(expectedText) {
      button => assertElementContainsId(button, id)
    }

  private def pageWithButton(expectedText: String)(additionalAssertions: Element => Assertion*): Unit =
    s"must render $expectedText button" in {
      val button = doc.getElementsByClass("govuk-button").toList.find(_.text() == expectedText).value
      additionalAssertions.map(_(button))
    }

  def pageWithLink(id: String, expectedText: String, expectedHref: String): Unit =
    pageWithLink(doc, id, expectedText, expectedHref)

  def pageWithLink(doc: Document, id: String, expectedText: String, expectedHref: String): Unit =
    s"must render link with id $id" in {
      val link = getElementById(doc, id)
      assertElementHasLink(link, expectedText, expectedHref)
    }

  def pageWithoutLink(doc: Document, id: String): Unit =
    s"must not render link with id $id" in {
      assertNotRenderedById(doc, id)
    }

  def pageWithBackLink(): Unit =
    "must render back link" in {
      val link = getElementByClass(doc, "govuk-back-link")
      assertElementContainsText(link, "Back")
      assertElementContainsHref(link, "#")
    }

  def pageWithoutBackLink(): Unit =
    "must not render back link" in {
      assertElementDoesNotExist(doc, "govuk-back-link")
    }

  def pageWithContent(tag: String, expectedText: String): Unit =
    pageWithContent(doc, tag, expectedText)

  def pageWithContent(doc: Document, tag: String, expectedText: String): Unit =
    pageWithContent(doc, tag, expectedText, _ `equals` _)

  def pageWithPartialContent(tag: String, expectedText: String): Unit =
    pageWithPartialContent(doc, tag, expectedText)

  def pageWithPartialContent(doc: Document, tag: String, expectedText: String): Unit =
    pageWithContent(doc, tag, expectedText, _ `contains` _)

  private def pageWithContent(doc: Document, tag: String, expectedText: String, condition: (String, String) => Boolean): Unit =
    s"must render $tag with text $expectedText" in {
      val elements = getElementsByTag(doc, tag)
      assertElementExists(elements, element => condition(element.text, expectedText))
    }

  def pageWithoutContent(doc: Document, tag: String, expectedText: String): Unit =
    s"must not render $tag with text $expectedText" in {
      val elements = getElementsByTag(doc, tag)
      assertElementDoesNotExist(elements, _.text == expectedText)
    }

  def pageWithList(listClass: String, expectedListItems: String*): Unit =
    "must render list" in {
      val list      = getElementByClass(doc, listClass)
      val listItems = list.getElementsByTag("li")
      listItems.toList.map(_.text()) mustEqual expectedListItems
    }

  def pageWithLinkedList(doc: Document, listClass: String, expectedListItems: (String, String, String)*): Unit =
    "must render list with links" in {
      val list      = getElementByClass(doc, listClass)
      val listItems = list.getElementsByTag("li")
      listItems.toList.zipWithIndex.foreach {
        case (listItem, index) =>
          val expectedListItem = expectedListItems.toList(index)
          val link             = listItem.getElementById(expectedListItem._1)
          assertElementHasLink(link, expectedListItem._2, expectedListItem._3)
      }
    }

  def pageWithFormAction(expectedUrl: String): Unit =
    "must render form with action" in {
      val formAction = getElementByTag(doc, "form").attr("action")
      formAction mustEqual expectedUrl
    }

  def pageWithHiddenInput(doc: Document, value: String): Unit =
    "must render hidden input" in {
      val input = getElementsByTag(doc, "input")
      assert(input.attr("type") == "hidden")
      assert(input.attr("value") == value)
    }

  def pageWithNoInput(doc: Document): Unit =
    "must render no input" in {
      val inputs = getElementsByTag(doc, "input")
      assert(inputs.isEmpty)
    }
}
