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

package utils

import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.html.components._
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._

private[utils] class SummaryListRowHelper(implicit messages: Messages) {

  protected def formatAsText[T](answer: T): Content = s"$answer".toText

  protected def formatAsPassword(answer: String): Content = ("•" * answer.length).toText

  protected def buildRow(
    prefix: String,
    answer: Content,
    id: Option[String],
    call: Call,
    args: Any*
  ): SummaryListRow =
    buildSimpleRow(
      prefix = prefix,
      label = messages(s"$prefix.checkYourAnswersLabel", args*).toText,
      answer = answer,
      id = id,
      call = call,
      args = args*
    )

  protected def buildSimpleRow(
    prefix: String,
    label: Content,
    answer: Content,
    id: Option[String],
    call: Call,
    args: Any*
  ): SummaryListRow =
    SummaryListRow(
      key = Key(label),
      value = Value(answer),
      actions = Some(
        Actions(
          items = List(
            ActionItem(
              content = messages("site.edit").toText,
              href = call.url,
              visuallyHiddenText = Some(messages(s"$prefix.change.hidden", args*)),
              attributes = id.fold[Map[String, String]](Map.empty)(
                id => Map("id" -> id)
              )
            )
          )
        )
      )
    )

}
