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

package views.behaviours

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import org.scalatest.Assertion
import play.twirl.api.HtmlFormat
import views.viewBase.ViewSpecAssertions

import scala.collection.JavaConverters._

trait ViewBehaviours extends SpecBase with ViewSpecAssertions {

  def view: HtmlFormat.Appendable

  def parseView(view: HtmlFormat.Appendable): Document = Jsoup.parse(view.toString())

  lazy val doc: Document = parseView(view)

  val prefix: String

  val hasSignOutLink: Boolean = true

  if (hasSignOutLink) {
    "must render sign out link in header" in {
      val link = getElementByClass(doc, "hmrc-sign-out-nav__link")
      assertElementContainsText(link, "Sign out")
      assertElementContainsHref(
        link,
        "http://localhost:9553/bas-gateway/sign-out-without-state?continue=http://localhost:9514/feedback/manage-transit-movements"
      )
    }

    "must render timeout dialog" in {
      val metas = getElementsByTag(doc, "meta")
      assertElementExists(metas, _.attr("name") == "hmrc-timeout-dialog")
      assertElementExists(metas, _.attr("data-keep-alive-url") == "/manage-transit-movements/arrival/keep-alive")
    }
  } else {
    "must not render sign out link in header" in {
      assertElementDoesNotExist(doc, "hmrc-sign-out-nav__link")
    }

    "must not render timeout dialog" in {
      val metas = getElementsByTag(doc, "meta")
      assertElementDoesNotExist(metas, _.attr("name") == "hmrc-timeout-dialog")
    }
  }

  "must render service name link in header" in {
    val link = getElementByClass(doc, "hmrc-header__service-name--linked")
    assertElementContainsText(link, "Check your transit guarantee balance")
    assertElementContainsHref(link, "/check-transit-guarantee-balance/start-again")
  }

  "must append service to feedback link" in {
    val link = getElementBySelector(doc, ".govuk-phase-banner__text > .govuk-link")
    getElementHref(link) must endWith("?service=CTCTraders")
  }

  "must render accessibility statement link" in {
    val link = doc
      .select(".govuk-footer__inline-list-item > .govuk-footer__link")
      .asScala
      .find(_.text() == "Accessibility statement")
      .get

    getElementHref(link) must include("http://localhost:12346/accessibility-statement/manage-transit-movements?referrerUrl=")
  }

  "must not render language toggle" in {
    assertElementDoesNotExist(doc, "hmrc-language-select")
  }

  def pageWithTitle(args: Any*): Unit =
    pageWithTitle(doc, prefix, args: _*)

  def pageWithTitle(doc: Document, prefix: String, args: Any*): Unit =
    "must render title" in {
      val title = doc.title()
      title mustBe s"${messages(s"$prefix.title", args: _*)} - Check your transit guarantee balance - GOV.UK"
    }

  def pageWithHeading(args: Any*): Unit =
    pageWithHeading(doc, prefix, args: _*)

  def pageWithHeading(doc: Document, prefix: String, args: Any*): Unit =
    "must render heading" in {
      val heading = getElementByTag(doc, "h1")
      assertElementIncludesText(heading, messages(s"$prefix.heading", args: _*))
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
    pageWithButton(expectedText) {
      button => assertElementContainsId(button, "submit")
    }

  private def pageWithButton(expectedText: String)(additionalAssertions: Element => Assertion*): Unit =
    s"must render $expectedText button" in {
      val button = getElementByClass(doc, "govuk-button")
      assertElementContainsText(button, expectedText)
      additionalAssertions.map(_(button))
    }

  def pageWithLink(id: String, expectedText: String, expectedHref: String): Unit =
    s"must render link with id $id" in {
      val link = getElementById(doc, id)
      assertElementContainsText(link, expectedText)
      assertElementContainsHref(link, expectedHref)
    }

  def pageWithBackLink(): Unit =
    "must render back link" in {
      val link = getElementByClass(doc, "govuk-back-link")
      assertElementContainsText(link, "Back")
      assertElementContainsHref(link, "javascript:history.back()")
    }

  def pageWithoutBackLink(): Unit =
    "must not render back link" in {
      assertElementDoesNotExist(doc, "govuk-back-link")
    }

  def pageWithContent(tag: String, expectedText: String): Unit =
    pageWithContent(doc, tag, expectedText)

  def pageWithContent(doc: Document, tag: String, expectedText: String): Unit =
    pageWithContent(doc, tag, expectedText, _ equals _)

  def pageWithPartialContent(tag: String, expectedText: String): Unit =
    pageWithContent(doc, tag, expectedText, _ contains _)

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
      listItems.asScala.map(_.text()) mustEqual expectedListItems
    }

  def pageWithFormAction(expectedUrl: String): Unit =
    "must render form with action" in {
      val formAction = getElementByTag(doc, "form").attr("action")
      formAction mustBe expectedUrl
    }
}
