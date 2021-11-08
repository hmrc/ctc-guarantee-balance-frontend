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

package navigation

import controllers.routes
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private val normalRoutes: Page => UserAnswers => Call = {
    commonRoutes(NormalMode) orElse {
      case _ => _ => routes.StartController.start()
    }
  }

  private val checkRoutes: Page => UserAnswers => Call = {
    commonRoutes(CheckMode) orElse {
      case _ => _ => routes.CheckYourAnswersController.onPageLoad()
    }
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRoutes(page)(userAnswers)
  }

  private def commonRoutes(mode: Mode): PartialFunction[Page, UserAnswers => Call] = {
    case EoriNumberPage               => _ => routes.GuaranteeReferenceNumberController.onPageLoad(mode)
    case GuaranteeReferenceNumberPage => _ => routes.AccessCodeController.onPageLoad(mode)
    case AccessCodePage               => _ => routes.CheckYourAnswersController.onPageLoad()
  }
}
