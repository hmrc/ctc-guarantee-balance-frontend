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

package views.viewBase

import org.jsoup.nodes.{Document, Element}
import org.jsoup.select.Elements

trait ViewSpecGetters {

  def getElementByClass(doc: Document, className: String): Element =
    getElementBySelector(doc, s".$className")

  def getElementById(doc: Document, id: String): Element =
    getElementBySelector(doc, s"#$id")

  def getElementBySelector(doc: Document, selector: String): Element =
    doc.select(selector).first()

  def getElementsByTag(doc: Document, tag: String): Elements =
    doc.getElementsByTag(tag)

  def getElementByTag(doc: Document, tag: String): Element =
    getElementsByTag(doc, tag).first()

  def getElementHref(element: Element): String =
    element.attr("href")
}
