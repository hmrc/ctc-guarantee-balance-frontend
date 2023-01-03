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

package forms

object Constants {

  lazy val maxEoriNumberLength: Int               = 17
  lazy val minEoriNumberLength: Int               = 14
  lazy val maxGuaranteeReferenceNumberLength: Int = 17
  lazy val accessCodeLength: Int                  = 4

  lazy val alphaNumericRegex: String     = "^[a-zA-Z0-9]*$"
  lazy val eoriNumberPrefixRegex: String = "^(?i)(gb|xi).*$"
  lazy val eoriNumberRegex: String       = "^(?i)(gb|xi)[0-9]*$"

}
