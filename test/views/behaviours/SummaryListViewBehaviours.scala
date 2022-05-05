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

import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryList

import scala.collection.JavaConverters._

trait SummaryListViewBehaviours extends ViewBehaviours {

  def summaryLists: Seq[SummaryList]

  // scalastyle:off method.length
  def pageWithSummaryLists(): Unit =
    "page with summary lists" - {

      val renderedLists = doc.getElementsByClass("govuk-summary-list").asScala

      summaryLists.zipWithIndex.foreach {
        case (summaryList, listIndex) =>
          val renderedList = renderedLists(listIndex)

          s"list ${listIndex + 1}" - {

            val renderedRows = renderedList.getElementsByClass("govuk-summary-list__row").asScala

            summaryList.rows.zipWithIndex.foreach {
              case (row, rowIndex) =>
                val renderedRow = renderedRows(rowIndex)
                s"row ${rowIndex + 1}" - {

                  "must contain a key" in {
                    val key = renderedRow.getElementsByClass("govuk-summary-list__key").text()
                    Text(key) mustBe row.key.content
                  }

                  "must contain a value" in {
                    val value = renderedRow.getElementsByClass("govuk-summary-list__value").text()
                    Text(value) mustBe row.value.content
                  }

                  row.actions match {
                    case None =>
                      "must not render any actions" in {
                        assert(renderedRow.getElementsByClass("govuk-summary-list__actions").isEmpty)
                      }
                    case Some(value) =>
                      val actions = renderedRow
                        .getElementsByClass("govuk-summary-list__actions")
                        .first()
                        .getElementsByClass("govuk-link")

                      value.items.zipWithIndex.foreach {
                        case (item, itemIndex) =>
                          s"must contain action ${itemIndex + 1}" in {
                            assertElementExists(
                              actions,
                              element =>
                                element.attr("href") == item.href && (item.visuallyHiddenText match {
                                  case Some(value) => element.getElementsByClass("govuk-visually-hidden").text() == value
                                  case None        => Text(element.text()) == item.content
                                })
                            )
                          }
                      }
                  }
                }
            }
          }
      }
    }
  // scalastyle:on method.length
}
