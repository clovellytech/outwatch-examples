import sbt._

object dependencies {
  val addResolvers = Seq(
    Resolver.sonatypeRepo("releases"),
    "jitpack" at "https://jitpack.io"
  )
  
  val compilerPlugins = Seq(
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9"),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.0-M4"),
  )

  val versions = new {
    val outwatch = "e0f28a8fbb"
    val outwatchExtras = "68aa33ac88"
    val scalaTest = "3.0.5"
  }
}
