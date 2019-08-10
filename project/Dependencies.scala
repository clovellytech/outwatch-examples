import sbt._

object dependencies {
  val addResolvers = Seq(
    Resolver.sonatypeRepo("releases"),
    "jitpack" at "https://jitpack.io"
  )

  val compilerPlugins = Seq(
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
  )

  val versions = new {
    val outwatch = "11f8c40"
    val scalaTest = "3.0.7"
  }
}
