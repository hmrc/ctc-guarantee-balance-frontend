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

package models

import base.SpecBase
import models.OptionBinder._
import org.scalatest.EitherValues
import play.api.mvc.PathBindable

class OptionBinderSpec extends SpecBase with EitherValues {

  "OptionBinder" - {

    "must bind value to Right as Some" in {

      val pathBindable = implicitly[PathBindable[Option[Int]]]

      val bind: Either[String, Option[Int]] = pathBindable.bind("foo", "123")
      bind.value.value mustEqual 123
    }

    "must bind value to Left" in {

      val pathBindable = implicitly[PathBindable[Option[Int]]]

      val bind: Either[String, Option[Int]] = pathBindable.bind("foo", "Invalid value")

      bind.isLeft mustEqual true
    }

    "must unbind path" in {

      val pathBindable = implicitly[PathBindable[Option[Int]]]
      val bindValue    = pathBindable.unbind("foo", Some(123))

      bindValue mustEqual "123"
    }
  }

}
