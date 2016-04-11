import Versions._
import sbt._
import Keys._

object Dependencies {
  val scalaReflect = (scalaVersion: String) => "org.scala-lang" %
    "scala-reflect" % scalaVersion
  val scalamock = "org.scalamock" %% "scalamock-scalatest-support" % "3.2"
  val scalatest = "org.scalatest" %% "scalatest" % "2.2.4"
  val scalaz = "org.scalaz" %% "scalaz-core" % "7.1.4"
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % akkaVersion
  val akkaContrib = "com.typesafe.akka" %% "akka-contrib" % akkaVersion
  val akkaPersistenceExperimental = "com.typesafe.akka" %% "akka-persistence-query-experimental" % akkaVersion
  val akkaTestKit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
  val akkaPersistenceInMemory = "com.github.dnvriend" %% "akka-persistence-inmemory" % "1.2.12" % Test

  val levelDb = "org.iq80.leveldb" % "leveldb" % "0.7"
  val levelDbJni = "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"


  val default = (scalaVersion: String) => Seq(
    scalaz,
    scalatest % Test,
    scalamock % Test,
    scalaReflect(scalaVersion) % Test
  )

  val akka = Seq(
    akkaActor,
    akkaPersistence,
    akkaContrib,
    akkaPersistenceExperimental
  )

  val akkaTest = Seq(
    akkaTestKit,
    akkaPersistenceInMemory
  )

  val levelDatabase = Seq(
    levelDb,
    levelDbJni
  )
}

object Versions {
  val akkaVersion = "2.4.3"
}