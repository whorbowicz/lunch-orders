import sbt._

object Dependencies {
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.4"
  val scalaz = "org.scalaz" %% "scalaz-core" % "7.1.4"


  val default = Seq(
    scalaz,
    scalatest % Test
  )
}