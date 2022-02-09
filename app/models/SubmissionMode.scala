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

package models

import play.api.libs.json.{JsString, Writes}
import play.api.mvc.JavascriptLiteral

sealed trait SubmissionMode

case object SubmitMode extends SubmissionMode {
  override def toString: String = "SubmitMode"
}

case object PollMode extends SubmissionMode {
  override def toString: String = "PollMode"
}

object SubmissionMode {
  implicit val jsLiteral: JavascriptLiteral[SubmissionMode] = (mode: SubmissionMode) => s""""$mode""""

  implicit def writes[T <: SubmissionMode]: Writes[T] = Writes {
    submissionMode => JsString(submissionMode.toString)
  }
}
