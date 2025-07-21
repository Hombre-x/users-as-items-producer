ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.1"

Compile / run / fork := true

lazy val root = (project in file("."))
  .settings(
    name             := "users-as-items-producer",
    idePackagePrefix := Some("com.mycode"),
    libraryDependencies ++= List(
      "org.typelevel"   %% "toolkit"   % "0.1.29",
      "com.github.fd4s" %% "fs2-kafka" % "3.8.0",
      "is.cir"          %% "ciris"     % "3.9.0"
    )
  )
