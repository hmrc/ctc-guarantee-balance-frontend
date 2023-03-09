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

package views.viewBase

import base.SpecBase
import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements
import org.scalatest.Assertion
import play.twirl.api.TwirlHelperImports._

trait ViewSpecAssertions extends ViewSpecGetters {
  this: SpecBase =>

  def assertRenderedById(doc: Document, id: String): Assertion =
    assert(doc.getElementById(id) != null, "\n\nElement " + id + " was not rendered on the page.\n")

  def assertNotRenderedById(doc: Document, id: String): Assertion =
    assert(doc.getElementById(id) == null, "\n\nElement " + id + " was rendered on the page.\n")

  def assertRenderedByCssSelector(doc: Document, cssSelector: String): Assertion =
    assert(!doc.select(cssSelector).isEmpty, "Element " + cssSelector + " was not rendered on the page.")

  def assertElementHasLink(element: Element, expectedText: String, expectedHref: String): Assertion = {
    assertElementContainsText(element, expectedText)
    assertElementContainsHref(element, expectedHref)
  }

  def assertElementContainsText(element: Element, expectedText: String): Assertion =
    element.text() mustBe expectedText

  def assertElementIncludesText(element: Element, expectedText: String): Assertion =
    element.text() must include(expectedText)

  def assertElementContainsHref(element: Element, expectedHref: String): Assertion =
    getElementHref(element) mustBe expectedHref

  def assertElementContainsId(element: Element, expectedId: String): Assertion =
    element.id() mustBe expectedId

  def assertElementExists(elements: Elements, condition: Element => Boolean): Assertion =
    assert(elements.toList.exists(condition))

  def assertElementDoesNotExist(elements: Elements, condition: Element => Boolean): Assertion =
    assert(!elements.toList.exists(condition))

  def assertElementDoesNotExist(doc: Document, className: String): Assertion =
    assert(doc.getElementsByClass(className).isEmpty)

  def assertRenderedByClass(doc: Document, className: String): Assertion =
    assert(!doc.getElementsByClass(className).isEmpty, "\n\nElement with class " + className + " was not rendered on the page.\n")

  def assertNotRenderedByClass(doc: Document, className: String): Assertion =
    assert(doc.getElementsByClass(className).isEmpty, "\n\nElement with class " + className + " was rendered on the page.\n")
}
