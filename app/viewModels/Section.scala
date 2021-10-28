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

package viewModels

import play.api.i18n.Messages
import play.api.libs.functional.syntax._
import play.api.libs.json.{__, OWrites}
import uk.gov.hmrc.viewmodels.SummaryList.Row
import uk.gov.hmrc.viewmodels.Text

case class Section(sectionTitle: Option[Text], rows: Seq[Row])

object Section {
  def apply(sectionTitle: Text, rows: Seq[Row]): Section = new Section(Some(sectionTitle), rows)

  def apply(rows: Seq[Row]): Section = new Section(None, rows)

  implicit def sectionWrites(implicit messages: Messages): OWrites[Section] =
    (
      (__ \ "sectionTitle").write[Option[Text]] and
        (__ \ "rows").write[Seq[Row]]
    )(unlift(Section.unapply))

}
