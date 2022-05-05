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

package views.utils

import play.api.i18n.Messages
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.implicits.{RichCharacterCountSupport, RichRadiosSupport, RichTextareaSupport}
import uk.gov.hmrc.govukfrontend.views.viewmodels.charactercount.CharacterCount
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.Radios
import uk.gov.hmrc.govukfrontend.views.viewmodels.textarea.Textarea

object ViewUtils {

  def breadCrumbTitle(title: String, mainContent: Html)(implicit messages: Messages): String =
    (if (mainContent.body.contains("govuk-error-summary")) s"${messages("error.title.prefix")} " else "") +
      s"$title - ${messages("site.service_name")} - GOV.UK"

  // TODO refactor this maybe? Going to need this for every ViewModel type going forward

  implicit class RadiosImplicits(radios: Radios)(implicit messages: Messages) extends RichRadiosSupport {

    def withHeadingAndCaption(heading: String, caption: Option[String]): Radios =
      caption match {
        case Some(value) => radios.withHeadingAndSectionCaption(Text(heading), Text(value))
        case None        => radios.withHeading(Text(heading))
      }

    def withLabel(label: String, labelIsVisible: Boolean = true): Radios = {
      val labelClass = if (labelIsVisible) "govuk-fieldset__legend--m" else "govuk-visually-hidden"
      radios.copy(
        fieldset = Some(Fieldset(legend = Some(Legend(content = Text(label), classes = labelClass, isPageHeading = false))))
      )
    }
  }

  implicit class TextAreaImplicits(textArea: Textarea)(implicit messages: Messages) extends RichTextareaSupport {

    def withHeadingAndCaption(heading: String, caption: Option[String]): Textarea =
      caption match {
        case Some(value) => textArea.withHeadingAndSectionCaption(Text(heading), Text(value))
        case None        => textArea.withHeading(Text(heading))
      }
  }

  implicit class CharacterCountImplicits(characterCount: CharacterCount)(implicit messages: Messages) extends RichCharacterCountSupport {

    def withHeadingAndCaption(heading: String, caption: Option[String]): CharacterCount =
      caption match {
        case Some(value) => characterCount.withHeadingAndSectionCaption(Text(heading), Text(value))
        case None        => characterCount.withHeading(Text(heading))
      }
  }

}
