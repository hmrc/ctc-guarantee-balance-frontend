import sbt._

object AppDependencies {

  private val mongoVersion = "0.73.0"
  private val bootstrapVersion = "7.8.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-play-28"              % mongoVersion,
    "uk.gov.hmrc"                %% "play-conditional-form-mapping"   % "1.12.0-play-28",
    "uk.gov.hmrc"                %% "bootstrap-frontend-play-28"      % bootstrapVersion,
    "uk.gov.hmrc"                %% "play-allowlist-filter"           % "1.1.0",
    "uk.gov.hmrc"                %% "play-frontend-hmrc"              % "3.32.0-play-28",
    "org.typelevel"              %% "cats-core"                       % "2.8.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"         %% "hmrc-mongo-test-play-28"  % mongoVersion,
    "org.scalatest"             %% "scalatest"                % "3.2.14",
    "uk.gov.hmrc"               %% "bootstrap-test-play-28"   % bootstrapVersion,
    "org.mockito"               %  "mockito-core"             % "4.8.0",
    "org.scalatestplus"         %% "mockito-4-6"              % "3.2.14.0",
    "org.scalacheck"            %% "scalacheck"               % "1.17.0",
    "org.scalatestplus"         %% "scalacheck-1-17"          % "3.2.14.0",
    "io.github.wolfendale"      %% "scalacheck-gen-regexp"    % "1.0.0",
    "org.jsoup"                 %  "jsoup"                    % "1.15.3",
    "com.github.tomakehurst"    %  "wiremock-standalone"      % "2.27.2",
    "com.vladsch.flexmark"      %  "flexmark-all"             % "0.62.2"
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
