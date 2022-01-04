/*
 * Copyright 2022 HM Revenue & Customs
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

package matchers

import org.scalatest.exceptions.TestFailedException
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatest.{OptionValues, Succeeded}
import play.api.libs.json.Json

class JsonMatchersSpec extends AnyFreeSpec with JsonMatchers with OptionValues {

  "contain Json" - {

    "must return Succeeded when expected Json is an empty Json object" in {

      val json         = Json.obj("foo" -> "bar")
      val expectedJson = Json.obj()

      val result = json must containJson(expectedJson)

      result mustBe Succeeded
    }

    "must return Succeeded when all keys in the expected Json are present" in {

      val json = Json.obj(
        "foo" -> 1,
        "bar" -> "baz"
      )

      val expectedJson = Json.obj("foo" -> 1)

      val result = json must containJson(expectedJson)

      result mustBe Succeeded
    }

    "must return Succeeded when both Json objects are the same" in {

      val json = Json.obj(
        "foo" -> 1,
        "bar" -> "baz"
      )

      val result = json must containJson(json)

      result mustBe Succeeded
    }

    "must throw a Test Failed Exception when expected Json contains a key not present in the left Json" in {

      val json         = Json.obj("foo" -> 1)
      val expectedJson = Json.obj("bar" -> "baz")

      val ex = intercept[TestFailedException] {
        json must containJson(expectedJson)
      }

      ex.message.value mustBe """{"foo":1} did not match for key(s) bar"""
    }

    "must throw a Test Failed Exception when expected Json contains a different value for a key in the left Json" in {

      val json         = Json.obj("foo" -> 1)
      val expectedJson = Json.obj("foo" -> 2)

      val ex = intercept[TestFailedException] {
        json must containJson(expectedJson)
      }

      ex.message.value mustBe """{"foo":1} did not match for key(s) foo"""
    }
  }
}
