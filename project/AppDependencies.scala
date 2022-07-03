import sbt._

object AppDependencies {

  private val mongoVersion = "0.66.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-play-28"              % mongoVersion,
    "uk.gov.hmrc"                %% "play-conditional-form-mapping"   % "1.11.0-play-28",
    "uk.gov.hmrc"                %% "bootstrap-frontend-play-28"      % "5.25.0",
    "uk.gov.hmrc"                %% "play-allowlist-filter"           % "1.1.0",
    "uk.gov.hmrc"                %% "play-frontend-hmrc"              % "3.21.0-play-28",
    "org.typelevel"              %% "cats-core"                       % "2.7.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"         %% "hmrc-mongo-test-play-28"  % mongoVersion,
    "org.scalatest"             %% "scalatest"                % "3.2.12",
    "org.scalatestplus.play"    %% "scalatestplus-play"       % "5.1.0",
    "org.scalatestplus"         %% "mockito-4-5"              % "3.2.12.0",
    "org.mockito"               %  "mockito-core"             % "4.6.1",
    "org.scalatestplus"         %% "scalacheck-1-16"          % "3.2.12.0",
    "org.scalacheck"            %% "scalacheck"               % "1.16.0",
    "wolfendale"                %% "scalacheck-gen-regexp"    % "0.1.2",
    "org.jsoup"                 %  "jsoup"                    % "1.14.3",
    "com.github.tomakehurst"    %  "wiremock-standalone"      % "2.27.2",
    "com.vladsch.flexmark"      %  "flexmark-all"             % "0.62.2"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
