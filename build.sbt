import dependencies._

cancelable in Global := true

val commonSettings = Seq(
  organization := "com.clovellytech",
  version := Version.version,
  scalaVersion := Version.scalaVersion,
  resolvers ++= addResolvers,
  scalacOptions ++= options.scalac,
  scalacOptions in (Compile, console) := options.scalacConsole,
) ++ compilerPlugins

lazy val docs = (project in file("./docs"))
  .settings(name := "todo-mvc")
  .settings(commonSettings)
  .enablePlugins(MdocPlugin)
  .settings(
    mdocVariables := Map(
    "VERSION" -> version.value
  ))
  .dependsOn(todomvc)

lazy val copyFastOptJS = TaskKey[Unit]("copyFastOptJS", "Copy javascript files to target directory")

lazy val todomvc  = (project in file("./todomvc"))
  .settings(name := "outwatch-examples-todomvc")
  .enablePlugins(ScalaJSPlugin)
  .enablePlugins(ScalaJSBundlerPlugin)
  .settings(
    commonSettings,
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
    libraryDependencies ++= Seq(
      "io.github.outwatch" % "outwatch" % versions.outwatch,
      "com.github.mariusmuja.outwatch-extras" % "outwatch-extras_sjs0.6_2.12" % versions.outwatchExtras,
      "org.scalatest" %%% "scalatest" % versions.scalaTest % Test
    ),
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
    addCommandAlias("devwatch", "~; fastOptJS; copyFastOptJS")
  )


lazy val root = (project in file("."))
  .settings(name := "outwatch-examples")
  .dependsOn(todomvc)
