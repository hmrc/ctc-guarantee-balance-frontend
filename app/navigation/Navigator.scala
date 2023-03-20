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

package navigation

import controllers.routes
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

sealed trait Navigator {
  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call
}

@Singleton
class NavigatorV1 @Inject() () extends Navigator {

  private val normalRoutes: Page => UserAnswers => Call = {
    case EoriNumberPage               => _ => routes.GuaranteeReferenceNumberController.onPageLoad(NormalMode)
    case GuaranteeReferenceNumberPage => _ => routes.AccessCodeController.onPageLoad(NormalMode)
    case AccessCodePage               => _ => routes.CheckYourAnswersControllerV1.onPageLoad()
    case _                            => _ => routes.StartController.startAgain()
  }

  private val checkRoutes: Page => UserAnswers => Call =
    _ => _ => routes.CheckYourAnswersControllerV1.onPageLoad()

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRoutes(page)(userAnswers)
  }
}

@Singleton
class NavigatorV2 @Inject() () extends Navigator {

  private val normalRoutes: Page => UserAnswers => Call = {
    case EoriNumberPage               => _ => routes.GuaranteeReferenceNumberController.onPageLoad(NormalMode)
    case GuaranteeReferenceNumberPage => _ => routes.AccessCodeController.onPageLoad(NormalMode)
    case AccessCodePage               => _ => routes.CheckYourAnswersControllerV2.onPageLoad()
    case _                            => _ => routes.StartController.startAgain()
  }

  private val checkRoutes: Page => UserAnswers => Call =
    _ => _ => routes.CheckYourAnswersControllerV2.onPageLoad()

  override def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRoutes(page)(userAnswers)
  }
}
