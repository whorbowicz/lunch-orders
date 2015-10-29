import sbt._

object Dependencies {
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.4"


  val default = Seq(
    scalatest % Test
  )
}