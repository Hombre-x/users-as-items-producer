ThisBuild / version := "0.1.1-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.3"

ThisBuild / semanticdbEnabled := true

lazy val root = (project in file("."))
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    name                 := "users-as-items-producer",
    idePackagePrefix     := Some("com.mycode"),
    Compile / run / fork := true,
    
    scalacOptions ++= List("-Wunused:all"),
    scalafixOnCompile    := true,

    // Docker settings for Podman
    dockerBaseImage      := "ghcr.io/graalvm/graalvm-community:25",
    dockerExposedPorts   := Seq(9000),
    dockerUpdateLatest   := true,
    dockerExecCommand    := Seq("podman"),
    dockerAlias          := DockerAlias(
      registryHost = None,
      username = Some("hombrexgsp"),
      name = "warehouse-producer",
      tag = Some((Docker / version).value)
    ),

    libraryDependencies ++= List(
      // Typelevel toolkit
      "org.typelevel" %% "toolkit" % "0.1.29",

      // Http4s
      "org.http4s" %% "http4s-dsl"                % "0.23.32",
      "org.http4s" %% "http4s-ember-server"       % "0.23.32",
      "org.http4s" %% "http4s-prometheus-metrics" % "0.25.0",

      // Kafka
      "com.github.fd4s" %% "fs2-kafka" % "3.9.0",

      // Skunk
      "org.tpolecat" %% "skunk-core" % "1.1.0-M3",

      // Ciris
      "is.cir" %% "ciris" % "3.11.1",

      // Scodec
      "org.scodec" %% "scodec-core" % "2.3.3",

      // Iron Types
      "io.github.iltotore" %% "iron"        % "3.2.0",
      "io.github.iltotore" %% "iron-cats"   % "3.2.0",
      "io.github.iltotore" %% "iron-circe"  % "3.2.0",
      "io.github.iltotore" %% "iron-skunk"  % "3.2.0",
      "io.github.iltotore" %% "iron-scodec" % "3.2.0",

      // Ducktape
      "io.github.arainko" %% "ducktape" % "0.2.10",

      // Java
      "ch.qos.logback" % "logback-classic" % "1.5.19"
    )
  )
