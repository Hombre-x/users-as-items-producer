ThisBuild / version := "0.1.1-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.3"

ThisBuild / semanticdbEnabled := true

ThisBuild / dockerExecCommand := Seq("podman")

Compile / run / fork := true

import Dependencies.Libraries

lazy val commonSettings = List(
  Compile / run / fork := true,
  scalacOptions ++= List("-Wunused:all"),
  scalafixOnCompile := false,
  scalafmtOnCompile := false,
  libraryDependencies ++= List(
    Libraries.toolkit,
    Libraries.ciris,
    Libraries.skunkCore,
    Libraries.iron,
    Libraries.ironCats,
    Libraries.ironCirce,
    Libraries.ironSkunk,
    Libraries.kittens,
    Libraries.logbackClassic
  )
)

def dockerSettings(name: String) = List(
  dockerBaseImage      := "ghcr.io/graalvm/graalvm-community:25",
  dockerExposedPorts   := Seq(9000),
  dockerUpdateLatest   := true,
  dockerExecCommand    := Seq("podman"),
  dockerAlias          := DockerAlias(
    registryHost = None,
    username = Some("hombrexgsp"),
    name = s"warehouse-producer-$name",
    tag = Some((Docker / version).value)
  ),
)

lazy val root = (project in file("."))
  .settings(
    name := "Warehouse",
  )
  .aggregate(common, persistence, publisher)

lazy val common = (project in file("modules/common"))
  .settings(commonSettings)
  .settings(
    name := "Warehouse.common",
    version := "0.1.1",
  )

lazy val persistence = (project in file("modules/persistence"))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .dependsOn(common)
  .settings(commonSettings)
  .settings(dockerSettings("persistence"))
  .settings(
    name := "persistence",
    version := "0.1.1",
    libraryDependencies ++= List(
      Libraries.http4sDsl,
      Libraries.http4sEmberServer,
      Libraries.http4sPrometheusMetrics,
      Libraries.circeGeneric,
      Libraries.ironScodec,
      Libraries.scodecCore,
      Libraries.ducktape,
    )
  )

lazy val publisher = (project in file("modules/publisher"))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .dependsOn(common)
  .settings(commonSettings)
  .settings(dockerSettings("publisher"))
  .settings(
    name := "publisher",
    version := "0.1.1",
    libraryDependencies ++= List(
      Libraries.circeGeneric,
      Libraries.circeParser,
      Libraries.fs2Kafka,
      Libraries.ironSkunk,
    )
  )


