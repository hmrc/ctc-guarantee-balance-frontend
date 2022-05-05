import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo"          %% "hmrc-mongo-play-28"              % "0.60.0",
    "uk.gov.hmrc"                %% "play-conditional-form-mapping"   % "1.11.0-play-28",
    "uk.gov.hmrc"                %% "bootstrap-frontend-play-28"      % "5.24.0",
    "uk.gov.hmrc"                %% "play-allowlist-filter"           % "1.0.0-play-28",
    "uk.gov.hmrc"                %% "play-nunjucks"                   % "0.35.0-play-28",
    "uk.gov.hmrc"                %% "play-nunjucks-viewmodel"         % "0.15.0-play-28",
    "org.webjars.npm"            % "govuk-frontend"                   % "4.0.1",
    "uk.gov.hmrc"                %% "play-frontend-hmrc"              % "3.15.0-play-28",
    "uk.gov.hmrc.webjars"        % "hmrc-frontend"                    % "4.5.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc.mongo"         %% "hmrc-mongo-test-play-28"  % "0.60.0",
    "org.scalatest"             %% "scalatest"                % "3.2.10",
    "org.scalatestplus"         %% "mockito-3-2"              % "3.1.2.0",
    "org.scalatestplus.play"    %% "scalatestplus-play"       % "5.1.0",
    "org.scalatestplus"         %% "scalatestplus-scalacheck" % "3.1.0.0-RC2",
    "org.jsoup"                 % "jsoup"                     % "1.14.3",
    "org.mockito"               % "mockito-core"              % "4.3.1",
    "org.scalacheck"            %% "scalacheck"               % "1.15.4",
    "com.github.tomakehurst"    %  "wiremock-standalone"      % "2.27.2",
    "wolfendale"                %% "scalacheck-gen-regexp"    % "0.1.2",
    "com.vladsch.flexmark"      %  "flexmark-all"             % "0.62.2"

  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
