import sbt._

object dependencies {
  val addResolvers = Seq(
    Resolver.sonatypeRepo("releases"),
    "jitpack" at "https://jitpack.io"
  )
  
  val compilerPlugins = Seq(
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9"),
  )

  val versions = new {
    val outwatch = "ea240c6d04"
    val outwatchExtras = "68aa33ac88"
    val scalaTest = "3.0.5"
  }
}
