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

import controllers.{CheckYourAnswersController, CheckYourAnswersControllerV1, DetailsDontMatchController, DetailsDontMatchControllerV1}
import forms.{GuaranteeReferenceNumberFormProvider, V1GuaranteeReferenceNumberFormProvider}
import handlers.{GuaranteeBalanceResponseHandler, GuaranteeBalanceResponseHandlerV1}
import navigation.{FirstPage, Navigator, NavigatorV1, V1FirstPage}
import services.{GuaranteeBalanceService, V1GuaranteeBalanceService}
import views.{V1ViewProvider, ViewProvider}

class V1Module extends Module {

  override def configure(): Unit = {
    super.configure()

    bind(classOf[ViewProvider]).to(classOf[V1ViewProvider])
    bind(classOf[GuaranteeBalanceService]).to(classOf[V1GuaranteeBalanceService])
    bind(classOf[GuaranteeReferenceNumberFormProvider]).to(classOf[V1GuaranteeReferenceNumberFormProvider])
    bind(classOf[FirstPage]).to(classOf[V1FirstPage])
    bind(classOf[CheckYourAnswersController]).to(classOf[CheckYourAnswersControllerV1])
    bind(classOf[Navigator]).to(classOf[NavigatorV1])
    bind(classOf[DetailsDontMatchController]).to(classOf[DetailsDontMatchControllerV1])
    bind(classOf[GuaranteeBalanceResponseHandler]).to(classOf[GuaranteeBalanceResponseHandlerV1])
  }

}
