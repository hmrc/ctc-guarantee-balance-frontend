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

package config

import controllers.{
  CheckYourAnswersController,
  CheckYourAnswersControllerV2,
  DetailsDontMatchController,
  DetailsDontMatchControllerV2,
  TryAgainController,
  TryAgainControllerV2
}
import forms.{GuaranteeReferenceNumberFormProvider, V2GuaranteeReferenceNumberFormProvider}
import handlers.{GuaranteeBalanceResponseHandler, GuaranteeBalanceResponseHandlerV2}
import navigation.{FirstPage, Navigator, NavigatorV2, V2FirstPage}
import services.{GuaranteeBalanceService, V2GuaranteeBalanceService}
import views.{V2ViewProvider, ViewProvider}

class V2Module extends Module {

  override def configure(): Unit = {
    super.configure()

    bind(classOf[ViewProvider]).to(classOf[V2ViewProvider])
    bind(classOf[GuaranteeBalanceService]).to(classOf[V2GuaranteeBalanceService])
    bind(classOf[GuaranteeReferenceNumberFormProvider]).to(classOf[V2GuaranteeReferenceNumberFormProvider])
    bind(classOf[FirstPage]).to(classOf[V2FirstPage])
    bind(classOf[CheckYourAnswersController]).to(classOf[CheckYourAnswersControllerV2])
    bind(classOf[Navigator]).to(classOf[NavigatorV2])
    bind(classOf[DetailsDontMatchController]).to(classOf[DetailsDontMatchControllerV2])
    bind(classOf[GuaranteeBalanceResponseHandler]).to(classOf[GuaranteeBalanceResponseHandlerV2])
    bind(classOf[TryAgainController]).to(classOf[TryAgainControllerV2])
  }

}
