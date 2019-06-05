import sbt._

object dependencies {
  val addResolvers = Seq(
    Resolver.sonatypeRepo("releases"),
    "jitpack" at "https://jitpack.io"
  )

  val compilerPlugins = Seq(
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.2"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0"),
  )

  val versions = new {
    val outwatch = "676f94a"
    val scalaTest = "3.0.7"
  }
}
