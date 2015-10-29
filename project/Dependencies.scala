import sbt._

object Dependencies {
  val scalaReflect = (scalaVersion: String) => "org.scala-lang" %
    "scala-reflect" % scalaVersion
  val scalamock = "org.scalamock" %% "scalamock-scalatest-support" % "3.2"
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.4"
  val scalaz = "org.scalaz" %% "scalaz-core" % "7.1.4"


  val default = (scalaVersion: String) => Seq(
    scalaz,
    scalatest % Test,
    scalamock % Test,
    scalaReflect(scalaVersion) % Test
  )
}