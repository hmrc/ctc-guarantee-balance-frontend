import play.sbt.routes.RoutesKeys
import sbt.Def
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "ctc-guarantee-balance-frontend"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala,  SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(itSettings) *)
  .settings(inConfig(IntegrationTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings) *)
  .configs(A11yTest)
  .settings(inConfig(A11yTest)(org.scalafmt.sbt.ScalafmtPlugin.scalafmtConfigSettings) *)
  .settings(DefaultBuildSettings.scalaSettings *)
  .settings(DefaultBuildSettings.defaultSettings() *)
  .settings(inConfig(Test)(testSettings) *)
  .settings(majorVersion := 0)
  .settings(headerSettings(IntegrationTest) *)
  .settings(automateHeaderSettings(IntegrationTest))
  .settings(headerSettings(A11yTest) *)
  .settings(automateHeaderSettings(A11yTest))
  .settings(scalaVersion := "3.5.0")
  .settings(
    name := appName,
    RoutesKeys.routesImport ++= Seq("models._", "models.OptionBinder._", "models.Referral._"),
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "play.twirl.api.HtmlFormat._",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "models.Mode",
      "controllers.routes._",
      "views.html.helper.CSPNonce",
      "viewModels.{InputSize, LabelSize, LegendSize}",
      "templates._"
    ),
    PlayKeys.playDefaultPort := 9462,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;.*repositories.*;" +
      ".*BuildInfo.*;.*javascript.*;.*Routes.*;.*GuiceInjector;" +
      ".*ControllerConfiguration",
    ScoverageKeys.coverageExcludedPackages := Seq(
      ".*testOnly.*",
      "views\\.html\\.components.*",
      "views\\.html\\.resources.*",
      "views\\.html\\.templates.*",
      "viewModels\\.audit\\.AuditConstants.*"
    ).mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 85,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting  := true,
    scalacOptions ++= Seq(
      "-feature",
      "-Wconf:src=routes/.*:s",
      "-Wconf:src=html/.*:s"
    ),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,
    resolvers ++= Seq(
      Resolver.jcenterRepo
    ),
    Concat.groups := Seq(
      "javascripts/application.js" -> group(Seq("javascripts/ctc.js"))
    ),
    Assets / pipelineStages := Seq(digest, concat),
    ThisBuild / useSuperShell := false,
    ThisBuild / scalafmtOnCompile := true
  )

lazy val testSettings: Seq[Def.Setting[?]] = Seq(
  fork := true,
  unmanagedResourceDirectories += baseDirectory.value / "test" / "resources",
  javaOptions ++= Seq(
    "-Dconfig.resource=test.application.conf"
  )
)

lazy val itSettings = Defaults.itSettings ++ Seq(
  unmanagedSourceDirectories := Seq(
    baseDirectory.value / "it"
  ),
  unmanagedResourceDirectories += baseDirectory.value / "it" / "resources",
  parallelExecution := false,
  fork              := true,
  javaOptions ++= Seq(
    "-Dconfig.resource=it.application.conf"
  )
)
