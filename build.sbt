import dependencies._
import sbt.Def
import sbt.Keys.name

cancelable in Global := true

val commonSettings = Seq(
  organization := "com.clovellytech",
  version := Version.version,
  scalaVersion := Version.scalaVersion,
  resolvers ++= addResolvers,
  scalacOptions ++= options.scalac,
  scalacOptions in (Compile, console) := options.scalacConsole,
  homepage := Some(url("https://clovellytech.github.io/outwatch-examples/index.html"))
) ++ compilerPlugins

val depSettings = Seq(
  libraryDependencies ++= Seq(
    "io.github.outwatch" % "outwatch" % versions.outwatch,
    "org.scalatest" %%% "scalatest" % versions.scalaTest % Test,
    "com.clovellytech" %%% "outwatch-router" % "0.0.5",
    "org.scala-js" %%% "scalajs-dom" % "0.9.6",
  ) ++ Seq(
    "circe-core",
    "circe-generic",
    "circe-parser",
  ).map("io.circe" %%% _ % "0.11.1")
)

val scalaJsSettings = Seq(
  scalaJSModuleKind := ModuleKind.CommonJSModule,
  scalacOptions += "-P:scalajs:sjsDefinedByDefault",
  useYarn := true, // makes scalajs-bundler use yarn instead of npm
  jsEnv in Test := new org.scalajs.jsenv.jsdomnodejs.JSDOMNodeJSEnv,
  scalaJSUseMainModuleInitializer := true,
  scalaJSModuleKind := ModuleKind.CommonJSModule, // configure Scala.js to emit a JavaScript module instead of a top-level script
  version in webpack := "4.16.1",
  version in startWebpackDevServer := "3.1.4",
  webpackDevServerExtraArgs := Seq("--progress", "--color"),
  webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack.config.dev.js"),
  // https://scalacenter.github.io/scalajs-bundler/cookbook.html#performance
  webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
  resolvers ++= addResolvers,
)

def jsProject(id: String, in: String): Project = Project(id, file(in))
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(commonSettings)
  .settings(scalaJsSettings)
  .settings(
    copyFastOptJS := {
      val inDir = (crossTarget in (Compile, fastOptJS)).value
      val outDir = (crossTarget in (Compile, fastOptJS)).value / "dev"
      val fileNames = Seq("-loader", "-library", "").map(x => s"${name.value}-fastopt$x.js")
      val copies = fileNames.map(p => (inDir / p, outDir / p))
      IO.copy(copies, overwrite = true, preserveLastModified = true, preserveExecutable = true)
    },
    // hot reloading configuration:
    // https://github.com/scalacenter/scalajs-bundler/issues/180
    addCommandAlias("dev", "; compile; fastOptJS::startWebpackDevServer; devwatch; fastOptJS::stopWebpackDevServer"),
    addCommandAlias("devwatch", "~; fastOptJS; copyFastOptJS"),
  )

lazy val todomvc = jsProject("outwatch-examples-todomvc", "./todomvc")
  .settings(depSettings)

lazy val auth = jsProject("outwatch-examples-auth", "./auth")
  .settings(depSettings)

lazy val examples = jsProject("outwatch-examples-directory", "./examples")
  .settings(depSettings)
  .dependsOn(todomvc, auth)

lazy val jsDocs = jsProject("outwatch-examples-docs", "./jsdocs")
  .settings(depSettings)
  .settings(
    mdocVariables := Map(
    "VERSION" -> version.value
  ))
  .settings(
    scalacOptions := options.scalacConsole
  )
  .dependsOn(todomvc, auth, examples)

lazy val docs = project
  .in(file("./ex-docs"))
  .settings(
    description := "A collection of examples demonstrating the outwatch scala.js virtual dom library",
    organizationName := "com.clovellytech",
    organizationHomepage := Some(url("https://github.com/clovellytech")),
    micrositeName := "Outwatch Examples",
    mdocJS := Some(jsDocs),
    micrositeCompilingDocsTool := WithMdoc,
  )
  .enablePlugins(MdocPlugin)
  .enablePlugins(MicrositesPlugin)


lazy val copyFastOptJS = TaskKey[Unit]("copyFastOptJS", "Copy javascript files to target directory")

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(name := "outwatch-examples")
