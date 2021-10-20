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

package pages.behaviours

import generators.Generators
import models.UserAnswers
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{OptionValues, TryValues}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.QuestionPage
import play.api.libs.json._

trait PageBehaviours extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators with OptionValues with TryValues {

  class BeRetrievable[A] {

    def apply[P <: QuestionPage[A]](genP: Gen[P])(implicit ev1: Arbitrary[A], ev2: Format[A]): Unit = {

      import models.RichJsObject

      "must return None" - {

        "when being retrieved from UserAnswers" - {

          "and the question has not been answered" in {

            val gen: Gen[(P, UserAnswers)] = for {
              page        <- genP
              userAnswers <- arbitrary[UserAnswers]
              json = userAnswers.data.removeObject(page.path).asOpt.getOrElse(userAnswers.data)
            } yield (page, userAnswers.copy(data = json))

            forAll(gen) {
              case (page, userAnswers) =>
                userAnswers.get(page) must be(empty)
            }
          }
        }
      }

      "must return the saved value" - {

        "when being retrieved from UserAnswers" - {

          "and the question has been answered" in {

            val gen = for {
              page        <- genP
              savedValue  <- arbitrary[A]
              userAnswers <- arbitrary[UserAnswers]
              json = userAnswers.data.setObject(page.path, Json.toJson(savedValue)).asOpt.value
            } yield (page, savedValue, userAnswers.copy(data = json))

            forAll(gen) {
              case (page, savedValue, userAnswers) =>
                userAnswers.get(page).value mustEqual savedValue
            }
          }
        }
      }
    }
  }

  class BeSettable[A] {

    def apply[P <: QuestionPage[A]](genP: Gen[P])(implicit ev1: Arbitrary[A], ev2: Format[A]): Unit =
      "must be able to be set on UserAnswers" in {

        val gen = for {
          page        <- genP
          newValue    <- arbitrary[A]
          userAnswers <- arbitrary[UserAnswers]
        } yield (page, newValue, userAnswers)

        forAll(gen) {
          case (page, newValue, userAnswers) =>
            val updatedAnswers = userAnswers.set(page, newValue).success.value
            updatedAnswers.get(page).value mustEqual newValue
        }
      }
  }

  class BeRemovable[A] {

    def apply[P <: QuestionPage[A]](genP: Gen[P])(implicit ev1: Arbitrary[A], ev2: Format[A]): Unit =
      "must be able to be removed from UserAnswers" in {

        val gen = for {
          page        <- genP
          savedValue  <- arbitrary[A]
          userAnswers <- arbitrary[UserAnswers]
        } yield (page, userAnswers.set(page, savedValue).success.value)

        forAll(gen) {
          case (page, userAnswers) =>
            val updatedAnswers = userAnswers.remove(page).success.value
            updatedAnswers.get(page) must be(empty)
        }
      }
  }

  def beRetrievable[A]: BeRetrievable[A] = new BeRetrievable[A]

  def beSettable[A]: BeSettable[A] = new BeSettable[A]

  def beRemovable[A]: BeRemovable[A] = new BeRemovable[A]
}
