package models

import play.api.libs.json.{JsString, Writes}
import play.api.mvc.JavascriptLiteral

sealed trait Mode

case object CheckMode extends Mode
case object NormalMode extends Mode

object Mode {

  implicit val jsLiteral: JavascriptLiteral[Mode] = {
    case NormalMode => """"NormalMode""""
    case CheckMode => """"CheckMode""""
  }

  implicit val writes: Writes[Mode] = Writes {
    case NormalMode => JsString("NormalMode")
    case CheckMode => JsString("CheckMode")
  }
}
