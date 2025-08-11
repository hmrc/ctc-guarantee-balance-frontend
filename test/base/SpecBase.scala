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

package base

import generators.Generators
import models.UserAnswers
import org.apache.pekko.stream.testkit.NoMaterializer
import org.scalatest.*
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import play.api.libs.json.{Json, Reads, Writes}
import play.api.mvc.{AnyContentAsEmpty, BodyParsers}
import play.api.test.FakeRequest
import play.api.test.Helpers.stubPlayBodyParsers
import uk.gov.hmrc.crypto.{Decrypter, Encrypter, SymmetricCryptoFactory}

import java.time.Instant

trait SpecBase
    extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with OptionValues
    with TryValues
    with ScalaFutures
    with MockitoSugar
    with Generators
    with EitherValues {

  private val encryptionKey                  = "zjZ7y/v1QTNlBlZJQCOfPygL+6dvuHGiGDF/ePvsZS0="
  implicit val crypto: Encrypter & Decrypter = SymmetricCryptoFactory.aesGcmCrypto(encryptionKey)

  val userAnswersId = "id"

  lazy val validEori: String = "GB1234567891234"

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId, Json.obj(), Instant.now())

  def fakeRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("", "")

  implicit val bodyParser: BodyParsers.Default = new BodyParsers.Default(stubPlayBodyParsers(NoMaterializer))

  implicit class RichUserAnswers(userAnswers: UserAnswers) {

    def getValue[T](page: QuestionPage[T])(implicit rds: Reads[T]): T =
      userAnswers.get(page).value

    def setValue[T](page: QuestionPage[T], value: T)(implicit rds: Reads[T], wts: Writes[T]): UserAnswers =
      userAnswers.set(page, value).success.value

    def setValue[T](page: QuestionPage[T], value: Option[T])(implicit rds: Reads[T], wts: Writes[T]): UserAnswers =
      value.map(setValue(page, _)).getOrElse(userAnswers)

    def removeValue(page: QuestionPage[?]): UserAnswers =
      userAnswers.remove(page).success.value
  }

}
