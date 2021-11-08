/*
 * Copyright 2021 HM Revenue & Customs
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

import org.jsoup.nodes.{Document, Element}
import play.api.libs.json.Json
import uk.gov.hmrc.viewmodels.SummaryList._
import uk.gov.hmrc.viewmodels.Text._
import viewModels.Section

import scala.collection.convert.ImplicitConversions._

class CheckYourAnswersViewSpec extends SingleViewSpec("checkYourAnswers.njk") {

  private val fakeSection = Section(
    (1 to 3).foldLeft[Seq[Row]](Nil) {
      (acc, i) =>
        acc :+ Row(Key(Literal(s"Key $i")), Value(Literal(s"Value $i")), Seq(Action(Literal(s"Action $i"), s"Link $i")))
    }
  )

  private val json = Json.obj(
    "section" -> Json.toJson(fakeSection)
  )

  override lazy val doc: Document = renderDocument(json).futureValue

  "must render correct heading" in {
    assertPageTitleEqualsMessage(doc, "checkYourAnswers.heading")
  }

  "must render correct caption" in {
    assertPageHasCaption(doc, "checkYourAnswers.preHeading")
  }

  "must render a continue button" in {
    assertPageHasButton(doc, "site.continue")
  }

  "must render a summary list" - {

    val summaryList: Element = doc.getElementsByClass("govuk-summary-list").first()
    val rows: List[Element]  = summaryList.getElementsByClass("govuk-summary-list__row").toList

    "must generate a row for each answer" in {
      rows.size() mustEqual fakeSection.rows.size
    }

    "must render correct data in each row" - {
      rows.zipWithIndex.foreach {
        case (row, rowIndex) =>
          s"when row ${rowIndex + 1}" - {

            "must have correct key" in {
              val key = row.getElementsByClass("govuk-summary-list__key").first()
              key.text() mustBe fakeSection.rows.get(rowIndex).key.content.asInstanceOf[Literal].resolve
            }

            "must have correct value" in {
              val value = row.getElementsByClass("govuk-summary-list__value").first()
              value.text() mustBe fakeSection.rows.get(rowIndex).value.content.asInstanceOf[Literal].resolve
            }

            "must have correct actions" - {

              val actions = row
                .getElementsByClass("govuk-summary-list__actions")
                .first()
                .getElementsByClass("govuk-link")
                .toList

              "must generate a link for each action" in {
                actions.size mustEqual fakeSection.rows.get(rowIndex).actions.size
              }

              "must render correct data for each action" - {
                actions.zipWithIndex.foreach {
                  case (action, actionIndex) =>
                    s"when action ${actionIndex + 1}" - {

                      "must render correct text" in {
                        action.text() mustBe fakeSection.rows.get(rowIndex).actions.get(actionIndex).content.asInstanceOf[Literal].resolve
                      }

                      "must render correct href" in {
                        action.attr("href") mustBe fakeSection.rows.get(rowIndex).actions.get(actionIndex).href
                      }
                    }
                }
              }
            }
          }
      }

    }
  }
}
