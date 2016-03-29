lazy val core = (project in file("core"))
  .settings(Settings.default: _*)
  .settings(
    name := "lunch-orders-core",
    libraryDependencies <++= scalaVersion(Dependencies.default))

lazy val coreAkka = (project in file("core-akka"))
  .settings(Settings.default: _*)
  .dependsOn(core)
  .settings(
    name := "lunch-orders-core-akka",
    libraryDependencies <++= scalaVersion(Dependencies.default),
    libraryDependencies ++= Dependencies.akka)

lazy val coreValidation = (project in file("core-validation"))
  .settings(Settings.default: _*)
  .dependsOn(core, coreAkka)
  .settings(
      name := "lunch-orders-core-validation",
      libraryDependencies <++= scalaVersion(Dependencies.default))

lazy val root = (project in file("."))
  .settings(Settings.default: _*)
  .settings(name := "lunch-orders")
  .aggregate(core, coreAkka, coreValidation)
