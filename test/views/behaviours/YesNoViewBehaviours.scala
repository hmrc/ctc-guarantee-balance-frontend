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
import uk.gov.hmrc.govukfrontend.views.viewmodels.radios.RadioItem

trait YesNoViewBehaviours extends RadioViewBehaviours[Boolean] {

  override def radioItems(fieldId: String, checkedValue: Option[Boolean]): Seq[RadioItem] =
    Seq(
      RadioItem(
        content = Text(messages("site.yes")),
        id = Some(fieldId),
        value = Some("true")
      ),
      RadioItem(
        content = Text(messages("site.no")),
        id = Some(s"$fieldId-no"),
        value = Some("false")
      )
    )

  override def values: Seq[Boolean] = Seq(true, false)
}
