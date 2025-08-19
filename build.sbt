ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "3.7.1"

lazy val root = (project in file("."))
  .settings(
    name                 := "users-as-items-producer",
    idePackagePrefix     := Some("com.mycode"),
    Compile / run / fork := true,
    libraryDependencies ++= List(
      // Typelevel toolkit
      "org.typelevel" %% "toolkit" % "0.1.29",

      // Http4s
      "org.http4s" %% "http4s-dsl"                % "0.23.30",
      "org.http4s" %% "http4s-ember-server"       % "0.23.30",
      "org.http4s" %% "http4s-prometheus-metrics" % "0.25.0",

      // Kafka
      "com.github.fd4s" %% "fs2-kafka" % "3.9.0",

      // Skunk
      "org.tpolecat" %% "skunk-core" % "1.1.0-M3",

      // Ciris
      "is.cir" %% "ciris" % "3.10.0",

      // Iron Types
      "io.github.iltotore" %% "iron" % "3.1.0",
      "io.github.iltotore" %% "iron-cats" % "3.1.0",
      "io.github.iltotore" %% "iron-circe" % "3.1.0",
      "io.github.iltotore" %% "iron-skunk" % "3.1.0",


      // Java
      "ch.qos.logback" % "logback-classic" % "1.5.18"
    )
  )
