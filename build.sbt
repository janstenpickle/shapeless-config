import microsites._
import sbt.Keys.libraryDependencies

val catsVer = "2.0.0"
val catsEffectVer = "2.0.0"
val circeVer = "0.12.1"
val collectionCompatVer = "2.1.2"
val disciplineScalaTestVer = "1.0.0-RC1"
val disciplineVer = "1.0.0"
val prometheusVer = "0.6.0"
val refinedVer = "0.9.9"
val scalaCheckVer = "1.14.2"
val scalaCheckShapelessVer = "1.2.3"
val scalaTestVer = "3.1.0-RC3"

def priorTo2_13(scalaVersion: String): Boolean =
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, minor)) if minor < 13 => true
    case _ => false
  }

lazy val macroSettings: Seq[Setting[_]] = Seq(
  libraryDependencies ++= Seq(
    scalaOrganization.value % "scala-compiler" % scalaVersion.value % Provided,
    scalaOrganization.value % "scala-reflect"  % scalaVersion.value % Provided
  ) ++ (
    if (priorTo2_13(scalaVersion.value)) {
      Seq(compilerPlugin(("org.scalamacros" % "paradise" % "2.1.1").cross(CrossVersion.patch)))
    } else Nil
  ),
  scalacOptions ++= (
    if (priorTo2_13(scalaVersion.value)) Seq("-Xfuture") else Seq("-Ymacro-annotations")
  )
)

lazy val commonSettings: Seq[Setting[_]] = Seq(
  organization := "io.extruder",
  scalaVersion := "2.13.5",
  crossScalaVersions := Seq("2.12.13", "2.13.5"),
  addCompilerPlugin(("org.typelevel" % "kind-projector" % "0.11.3").cross(CrossVersion.patch)),
  scalacOptions ++= Seq(
    "-deprecation",
    "-unchecked",
    "-feature",
    "-deprecation:false",
    "-Xcheckinit",
    "-Xlint:-nullary-unit",
    "-Ywarn-numeric-widen",
    "-Ywarn-dead-code",
    "-language:_",
    "-encoding",
    "UTF-8"
  ),
  publishMavenStyle := true,
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  homepage := Some(url("https://github.com/janstenpickle/extruder")),
  developers := List(
    Developer(
      "janstenpickle",
      "Chris Jansen",
      "janstenpickle@users.noreply.github.com",
      url = url("https://github.com/janstepickle")
    )
  ),
  publishArtifact in Test := false,
  pomIncludeRepository := { _ =>
    false
  },
  coverageMinimum := 80,
  coverageHighlighting := true,
  releaseCrossBuild := true,
  scalafmtOnCompile := true,
  scalafmtTestOnCompile := true,
  releaseIgnoreUntrackedFiles := true,
  parallelExecution in ThisBuild := true,
  logBuffered in Test := false,
  bintrayRepository := "extruder",
  bintrayReleaseOnPublish := false
) ++ macroSettings

lazy val core = (project in file("core"))
  .settings(
    commonSettings ++
      Seq(
        name := "extruder-core",
        libraryDependencies ++= Seq(
          "io.estatico"            %% "newtype"                 % "0.4.3",
          "org.typelevel"          %% "cats-core"               % catsVer,
          "org.typelevel"          %% "cats-laws"               % catsVer,
          "org.typelevel"          %% "mouse"                   % "0.22",
          "com.chuusai"            %% "shapeless"               % "2.3.3",
          "org.scala-lang.modules" %% "scala-collection-compat" % collectionCompatVer,
          "org.scalatest"          %% "scalatest"               % scalaTestVer % Test,
          "org.scalacheck"         %% "scalacheck"              % scalaCheckVer % Test,
          "org.typelevel"          %% "discipline-scalatest"    % disciplineScalaTestVer % Test,
          "org.typelevel"          %% "discipline-core"         % disciplineVer % Test,
          ("com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % scalaCheckShapelessVer % Test)
            .exclude("org.scalacheck", "scalacheck")
        ),
        publishArtifact in Test := true,
        coverageEnabled.in(Test, test) := true
      )
  )

lazy val laws = (project in file("laws"))
  .settings(
    commonSettings ++
      Seq(
        name := "extruder-laws",
        libraryDependencies ++= Seq(
          "org.scalatest"              %% "scalatest"                 % scalaTestVer,
          "org.typelevel"              %% "cats-laws"                 % catsVer,
          "org.typelevel"              %% "discipline-scalatest"      % disciplineScalaTestVer,
          "org.typelevel"              %% "discipline-core"           % disciplineVer,
          "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % scalaCheckShapelessVer
        ),
        coverageEnabled.in(Test, test) := true
      )
  )
  .dependsOn(core)

lazy val tests = (project in file("tests"))
  .settings(
    commonSettings ++
      Seq(
        name := "extruder-tests",
        libraryDependencies ++= Seq(
          "org.scalatest"              %% "scalatest"                 % scalaTestVer           % Test,
          "org.typelevel"              %% "discipline-scalatest"      % disciplineScalaTestVer % Test,
          "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % scalaCheckShapelessVer % Test
        ),
        coverageEnabled.in(Test, test) := true
      )
  )
  .dependsOn(testkit % "test")

lazy val testkit = project.dependsOn(core, laws).settings(commonSettings).settings(moduleName := "extruder-testkit")

lazy val catsEffect = (project in file("cats-effect"))
  .settings(
    commonSettings ++
      Seq(
        name := "extruder-cats-effect",
        libraryDependencies ++= Seq(
          "org.typelevel"              %% "cats-effect"               % catsEffectVer,
          "org.scalatest"              %% "scalatest"                 % scalaTestVer % Test,
          "org.typelevel"              %% "cats-effect-laws"          % catsEffectVer,
          "org.typelevel"              %% "discipline-core"           % disciplineVer % Test,
          "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % scalaCheckShapelessVer % Test
        )
      )
  )
  .dependsOn(core, laws)

lazy val systemSources = (project in file("system-sources"))
  .settings(commonSettings ++ Seq(name := "extruder-system-sources"))
  .dependsOn(core, catsEffect)

lazy val examples = (project in file("examples"))
  .settings(
    commonSettings ++
      Seq(name := "extruder-examples", publishArtifact := false)
  )
  .dependsOn(systemSources, typesafe, refined, circe)

lazy val aws = (project in file("aws"))
  .settings(
    commonSettings ++
      Seq(
        name := "extruder-aws",
        libraryDependencies ++= Seq(
          "eu.timepit"     %% "refined"            % refinedVer,
          "com.amazonaws"  % "aws-java-sdk-core"   % "1.11.568",
          "org.scalatest"  %% "scalatest"          % scalaTestVer % Test,
          "org.scalacheck" %% "scalacheck"         % scalaCheckVer % Test,
          "eu.timepit"     %% "refined-scalacheck" % refinedVer % Test
        ),
        coverageEnabled.in(Test, test) := true
      )
  )
  .dependsOn(core, laws)

lazy val circe = (project in file("circe"))
  .settings(
    commonSettings ++
      Seq(
        name := "extruder-circe",
        libraryDependencies ++= Seq(
          "io.circe"      %% "circe-core" % circeVer,
          "org.scalatest" %% "scalatest"  % scalaTestVer % Test
        ),
        coverageEnabled.in(Test, test) := true
      )
  )
  .dependsOn(core, laws % "test->compile")

lazy val circeYaml = (project in file("circe-yaml"))
  .settings(
    commonSettings ++
      Seq(
        name := "extruder-circe-yaml",
        libraryDependencies ++= Seq(
          "io.circe"      %% "circe-yaml" % "0.11.0-M1",
          "org.scalatest" %% "scalatest"  % scalaTestVer % Test
        ),
        coverageEnabled.in(Test, test) := true
      )
  )
  .dependsOn(circe, laws % "test->compile")

lazy val typesafe = (project in file("typesafe"))
  .settings(
    commonSettings ++
      Seq(
        name := "extruder-typesafe",
        libraryDependencies ++= Seq(
          "com.typesafe"           % "config"                   % "1.3.4",
          "org.scala-lang.modules" %% "scala-collection-compat" % collectionCompatVer,
          "org.scalatest"          %% "scalatest"               % scalaTestVer % Test,
          "org.scalacheck"         %% "scalacheck"              % scalaCheckVer % Test
        ),
        coverageEnabled.in(Test, test) := true
      )
  )
  .dependsOn(core, laws % "test->compile")

lazy val metricsCore = (project in file("metrics/core"))
  .settings(
    commonSettings ++
      Seq(
        name := "extruder-metrics-core",
        libraryDependencies ++= Seq(
          "org.scalatest"              %% "scalatest"                 % scalaTestVer           % Test,
          "org.scalacheck"             %% "scalacheck"                % scalaCheckVer          % Test,
          "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % scalaCheckShapelessVer % Test,
          "org.typelevel"              %% "discipline-core"           % disciplineVer          % Test
        ),
        coverageEnabled.in(Test, test) := true
      )
  )
  .dependsOn(core, catsEffect)

lazy val prometheus = (project in file("metrics/prometheus"))
  .settings(
    commonSettings ++
      Seq(
        name := "extruder-metrics-prometheus",
        libraryDependencies ++= Seq(
          "io.prometheus"              % "simpleclient"               % prometheusVer,
          "io.prometheus"              % "simpleclient_pushgateway"   % prometheusVer,
          "org.scalatest"              %% "scalatest"                 % scalaTestVer % Test,
          "org.scalacheck"             %% "scalacheck"                % scalaCheckVer % Test,
          "org.mockito"                %% "mockito-scala-scalatest"   % "1.7.1" % Test,
          "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % scalaCheckShapelessVer % Test
        ),
        coverageEnabled.in(Test, test) := true
      )
  )
  .dependsOn(metricsCore)

lazy val spectator = (project in file("metrics/spectator"))
  .settings(
    commonSettings ++
      Seq(
        name := "extruder-metrics-spectator",
        libraryDependencies ++= Seq(
          "com.netflix.spectator"      % "spectator-api"              % "0.85.0",
          "com.netflix.spectator"      % "spectator-reg-servo"        % "0.85.0" % Test,
          "org.scalatest"              %% "scalatest"                 % scalaTestVer % Test,
          "org.scalacheck"             %% "scalacheck"                % scalaCheckVer % Test,
          "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % scalaCheckShapelessVer % Test
        ),
        coverageEnabled.in(Test, test) := true
      )
  )
  .dependsOn(metricsCore, catsEffect)

lazy val dropwizard = (project in file("metrics/dropwizard"))
  .settings(
    commonSettings ++
      Seq(
        name := "extruder-metrics-dropwizard",
        libraryDependencies ++= Seq(
          "io.dropwizard.metrics5"     % "metrics-core"               % "5.0.0",
          "org.scalatest"              %% "scalatest"                 % scalaTestVer % Test,
          "org.scalacheck"             %% "scalacheck"                % scalaCheckVer % Test,
          "com.github.alexarchambault" %% "scalacheck-shapeless_1.14" % scalaCheckShapelessVer % Test
        ),
        coverageEnabled.in(Test, test) := true
      )
  )
  .dependsOn(metricsCore)

lazy val refined = (project in file("refined"))
  .settings(
    commonSettings ++
      Seq(
        name := "extruder-refined",
        libraryDependencies ++= Seq(
          "eu.timepit"     %% "refined"            % refinedVer,
          "eu.timepit"     %% "refined-shapeless"  % refinedVer,
          "eu.timepit"     %% "refined-scalacheck" % refinedVer % Test,
          "org.scalatest"  %% "scalatest"          % scalaTestVer % Test,
          "org.scalacheck" %% "scalacheck"         % scalaCheckVer % Test
        ),
        coverageEnabled.in(Test, test) := true
      )
  )
  .dependsOn(core, laws % "test->compile")

lazy val root = (project in file("."))
  .settings(
    commonSettings ++
      Seq(
        name := "extruder",
        unmanagedSourceDirectories in Compile := unmanagedSourceDirectories.all(aggregateCompile).value.flatten,
        sources in Compile := sources.all(aggregateCompile).value.flatten,
        libraryDependencies := libraryDependencies.all(aggregateCompile).value.flatten
      )
  )
  .aggregate(
    core,
    tests,
    laws,
    catsEffect,
    aws,
    typesafe,
    circe,
    circeYaml,
    systemSources,
    refined,
    metricsCore,
    dropwizard,
    prometheus,
    spectator
  )

lazy val aggregateCompile =
  ScopeFilter(inProjects(core), inConfigurations(Compile))

lazy val docSettings = commonSettings ++ Seq(
  micrositeName := "extruder",
  micrositeDescription := "Populate Scala case classes from any data source",
  micrositeAuthor := "Chris Jansen",
  micrositeHighlightTheme := "atom-one-light",
  micrositeHomepage := "https://janstenpickle.github.io/extruder/",
  micrositeBaseUrl := "extruder",
  micrositeDocumentationUrl := "api",
  micrositeGithubOwner := "janstenpickle",
  micrositeGithubRepo := "extruder",
  micrositeExtraMdFiles := Map(file("CONTRIBUTING.md") -> ExtraMdFileConfig("contributing.md", "docs")),
  micrositeFavicons := Seq(
    MicrositeFavicon("favicon16x16.png", "16x16"),
    MicrositeFavicon("favicon32x32.png", "32x32")
  ),
  micrositePalette := Map(
    "brand-primary" -> "#009933",
    "brand-secondary" -> "#006600",
    "brand-tertiary" -> "#339933",
    "gray-dark" -> "#49494B",
    "gray" -> "#7B7B7E",
    "gray-light" -> "#E5E5E6",
    "gray-lighter" -> "#F4F3F4",
    "white-color" -> "#FFFFFF"
  ),
  micrositePushSiteWith := GitHub4s,
  micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
  micrositeGitterChannel := false,
  micrositeCDNDirectives := CdnDirectives(
    jsList = List("https://cdn.rawgit.com/knsv/mermaid/6.0.0/dist/mermaid.min.js"),
    cssList = List("https://cdn.rawgit.com/knsv/mermaid/6.0.0/dist/mermaid.css")
  ),
  addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), micrositeDocumentationUrl),
  ghpagesNoJekyll := false,
  scalacOptions in (ScalaUnidoc, unidoc) ++= Seq(
    "-groups",
    "-implicits",
    "-skip-packages",
    "scalaz",
    "-sourcepath",
    baseDirectory.in(LocalRootProject).value.getAbsolutePath,
    "-doc-root-content",
    (resourceDirectory.in(Compile).value / "rootdoc.txt").getAbsolutePath
  ),
  git.remoteRepo := "git@github.com:janstenpickle/extruder.git",
  unidocProjectFilter in (ScalaUnidoc, unidoc) :=
    inAnyProject -- inProjects(root, examples, laws),
  includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.svg" | "*.js" | "*.swf" | "*.yml" | "*.md"
)

lazy val docs = project
  .dependsOn(core, systemSources, circe, typesafe, refined, metricsCore)
  .settings(
    moduleName := "extruder-docs",
    name := "Extruder docs",
    publish := (()),
    publishLocal := (()),
    publishArtifact := false,
    libraryDependencies += "io.circe" %% "circe-generic" % circeVer
  )
  .settings(docSettings)
  .enablePlugins(ScalaUnidocPlugin)
  .enablePlugins(GhpagesPlugin)
  .enablePlugins(MicrositesPlugin)
