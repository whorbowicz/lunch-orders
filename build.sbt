lazy val core = (project in file("core"))
  .settings(Settings.default: _*)
  .settings(
    name := "lunch-orders-core",
    libraryDependencies ++= Dependencies.default)

lazy val root = (project in file("."))
  .settings(Settings.default: _*)
  .settings(name := "lunch-orders")
  .aggregate(core)
