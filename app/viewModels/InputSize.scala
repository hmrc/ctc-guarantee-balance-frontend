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

package viewModels

import scala.language.implicitConversions

sealed abstract class InputSize(val className: String) {

  override val toString: String = className

}

object InputSize {
  case object Width20 extends InputSize("govuk-input--width-20")
  case object Width10 extends InputSize("govuk-input--width-10")
  case object Width5 extends InputSize("govuk-input--width-5")
  case object Width4 extends InputSize("govuk-input--width-4")
  case object Width3 extends InputSize("govuk-input--width-3")
  case object Width2 extends InputSize("govuk-input--width-2")

  implicit def sizeToOptionString(size: InputSize): Option[String] = Some(size.toString)
}
