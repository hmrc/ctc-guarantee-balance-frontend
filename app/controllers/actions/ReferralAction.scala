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

package controllers.actions

import com.google.inject.Inject
import models.Referral
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

trait ReferralActionProvider {

  def apply(referral: Referral): ReferralAction
}

class ReferralActionProviderImpl @Inject() (implicit executionContext: ExecutionContext, parser: BodyParsers.Default) extends ReferralActionProvider {

  override def apply(referral: Referral): ReferralAction = new ReferralAction(referral)
}

class ReferralAction(referral: Referral)(implicit val executionContext: ExecutionContext, val parser: BodyParsers.Default)
    extends ActionBuilder[Request, AnyContent] {

  override def invokeBlock[A](request: Request[A], block: Request[A] => Future[Result]): Future[Result] =
    block(request).map(_.withCookies(Cookie("referral", referral.toString)))
}
