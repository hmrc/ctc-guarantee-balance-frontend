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

package services

import models.Referral
import play.api.mvc.{Request, Result}

class ReferralService {

  def getReferralFromSession[A <: Request[_]](implicit request: A): Option[String] =
    request.session.get(Referral.key)

  def setReferralInSession[A <: Request[_]](result: Result, referral: Referral)(implicit request: A): Result =
    result.addingToSession(Referral.key -> referral.toString)(request)

}
