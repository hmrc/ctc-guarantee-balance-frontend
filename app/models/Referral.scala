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

import play.api.mvc.{JavascriptLiteral, QueryStringBindable}

sealed trait Referral

object Referral extends Enumerable.Implicits {

  case object GovUK extends WithName("govuk") with Referral

  case object NCTS extends WithName("ncts") with Referral

  lazy val cookieName: String = "referral"

  implicit val jsLiteral: JavascriptLiteral[Referral] = (referral: Referral) => s""""$referral""""

  val values: Set[Referral] = Set(
    GovUK,
    NCTS
  )

  implicit def enumerable[T <: Referral]: Enumerable[T] =
    Enumerable(
      values.toSeq.map(
        v => v.toString -> v.asInstanceOf[T]
      ): _*
    )

  implicit def queryStringBinder(implicit stringBinder: QueryStringBindable[String]): QueryStringBindable[Referral] = new QueryStringBindable[Referral] {

    override def bind(key: String, params: Map[String, Seq[String]]): Option[Either[String, Referral]] =
      stringBinder.bind(key, params) match {
        case Some(Right(referral)) =>
          referral.toLowerCase match {
            case x if x == GovUK.toString => Some(Right(GovUK))
            case x if x == NCTS.toString  => Some(Right(NCTS))
            case x                        => Some(Left(s"Invalid Referral Type: $x"))
          }
        case _ => None
      }

    override def unbind(key: String, Referral: Referral): String = stringBinder.unbind(key, Referral.toString)
  }
}
