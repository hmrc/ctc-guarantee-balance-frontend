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

package controllers

import controllers.actions._
import models.{CheckMode, UserAnswers}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{Format, Json, Reads}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import renderer.Renderer
import uk.gov.hmrc.http.HttpReads.{is2xx, is4xx}
//import uk.gov.hmrc.mongo.lock.MongoLockRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.viewmodels.NunjucksSupport
import utils.CheckYourAnswersHelper
import viewModels.Section

import javax.inject.Inject
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  getData: DataRetrievalAction,
  requireData: DataRequiredAction,
  val controllerComponents: MessagesControllerComponents,
  renderer: Renderer
  // mongoLockRepository: MongoLockRepository
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport
    with NunjucksSupport {

  def onPageLoad: Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val answers = createSections(request.userAnswers)
      val json = Json.obj(
        "section" -> Json.toJson(answers)
      )

      renderer.render("checkYourAnswers.njk", json).map(Ok(_))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      println("\n\n\n\nrequest" + request)
      val answers = createSections(request.userAnswers)
      val json = Json.obj(
        "section" -> Json.toJson(answers)
      )

      renderer.render("checkYourAnswers.njk", json).map(Ok(_))
//        val jsonGuarantees = Json.parse(request.userAnswers.data.value.getOrElse("guarantees", "").toString)
//
//        implicit val a: Format[guarantees] = Json.format[guarantees]
//        implicit val b: Reads[guarantees]  = Json.reads[guarantees]
//
//        val listOfGuarantees = jsonGuarantees.as[guarantees]
//        val owner            = java.util.UUID.randomUUID().toString
//        val duration         = 60.seconds
//
//        mongoLockRepository
//          .takeLock((request.userAnswers. listOfGuarantees.guaranteeReference.trim.toLowerCase).hashCode.toString, owner, duration)
//          .flatMap {
//            lockTaken =>
//              if (true) {
//                submissionService.submit(request.userAnswers) flatMap {
//
//                  case Right(value) =>
//                    value.status match {
//                      case status if is2xx(status) => Future.successful(Redirect(routes.SubmissionConfirmationController.onPageLoad(lrn)))
//                      case status if is4xx(status) => errorHandler.onClientError(request, status)
//                      case _                       => renderTechnicalDifficultiesPage
//                    }
//
//                  case Left(_) => // TODO we can pass this value back to help debug
//                    errorHandler.onClientError(request, BAD_REQUEST)
//                }
//              } else {
//                println("\n\n\n\n\n\n\n\nLockNotTking\n\n\n\n\n\n")
//                Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
//              }
//          }

  }

  //TODO onPost to be implemented once the backend is implemented
  //TODO call .removeSpaces() on GRN before sending to backend

  private def createSections(userAnswers: UserAnswers): Section = {
    val helper = new CheckYourAnswersHelper(userAnswers, CheckMode)

    Section(
      Seq(
        helper.eoriNumber,
        helper.guaranteeReferenceNumber,
        helper.accessCode
      ).flatten
    )
  }

  case class guarantees(
    guaranteeType: String,
    guaranteeReference: String,
    liabilityAmount: String,
    accessCode: String
  )

}
