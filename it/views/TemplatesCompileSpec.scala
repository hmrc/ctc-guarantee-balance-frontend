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

package views

import models.{Mode, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.select.Elements
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.Json
import play.api.mvc.RequestHeader
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest
import play.twirl.api.Html
import renderer.Renderer

import java.io.File
import scala.collection.JavaConverters._

class TemplatesCompileSpec extends AnyFreeSpec with Matchers with ScalaFutures with IntegrationPatience with GuiceOneAppPerSuite {

  def getListOfFiles(dir: String): List[File] = {
    val d = new File(dir)
    if (d.exists && d.isDirectory) {
      d.listFiles.flatMap {
        case file if file.isFile    => List(file)
        case dir if dir.isDirectory => getListOfFiles(dir.getPath)
      }.toList
    } else {
      List[File]()
    }
  }

  private val mode: Mode = NormalMode

  "must render all the templates" in {

    implicit val request: RequestHeader = FakeRequest("", "").withCSRFToken

    val renderer = app.injector.instanceOf[Renderer]

    val templates = getListOfFiles("conf/views")
    templates.map {
      filename =>
        note(s"Render $filename...")
        val path            = filename.toPath
        val pathInsideViews = path.subpath(2, path.getNameCount)
        val result = renderer.render(
          pathInsideViews.toString,
          Json.obj(
            "mode"      -> mode,
            "balanceId" -> "testBalanceId",
            "submitUrl" -> "submitAction"
          )
        )
        val html: Html = result.futureValue
        html mustBe an[Html]
        val document        = Jsoup.parse(html.toString())
        val forms: Elements = document.getElementsByTag("form")
        asScalaBuffer(forms).map {
          form =>
            val action = form.attr("action")
            action mustNot be("")
            action mustNot include("undefined")
        }
    }
  }
}
