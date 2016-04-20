lazy val core = (project in file("core"))
  .settings(Settings.default: _*)
  .settings(
    name := "lunch-orders-core",
    libraryDependencies <++= scalaVersion(Dependencies.default),
    libraryDependencies ++= Dependencies.akka ++ Dependencies.akkaTest)

lazy val coreValidation = (project in file("core-validation"))
  .settings(Settings.default: _*)
  .dependsOn(core)
  .settings(
    name := "lunch-orders-core-validation",
    libraryDependencies <++= scalaVersion(Dependencies.default),
    libraryDependencies ++= Dependencies.akkaTest)

lazy val root = (project in file("."))
  .settings(Settings.default: _*)
  .settings(name := "lunch-orders")
  .aggregate(core, coreValidation)
