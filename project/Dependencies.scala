import sbt.*

object Dependencies {
  private object Versions {
    val toolkit            = "0.1.29"
    val http4s             = "0.23.33"
    val http4sPrometheus   = "0.25.0"
    val fs2Kafka           = "3.9.1"
    val circe    = "0.14.15"
    val skunk    = "1.1.0-M3"
    val ciris    = "3.11.1"
    val scodec   = "2.3.3"
    val iron     = "3.2.1"
    val ducktape = "0.2.10"
    val kittens  = "3.5.0"

    // Java
    val logback = "1.5.21"
  }

  private object Organizations {
    val typelevel = "org.typelevel"
    val http4s    = "org.http4s"
    val fd4s      = "com.github.fd4s"
    val tpolecat  = "org.tpolecat"
    val circe     = "io.circe"
    val ciris     = "is.cir"
    val scodec    = "org.scodec"
    val iltotore  = "io.github.iltotore"
    val arainko   = "io.github.arainko"
    val logback   = "ch.qos.logback"
  }

  object Libraries {

    // Typelevel toolkit
    val toolkit = Organizations.typelevel %% "toolkit" % Versions.toolkit

    // Kittens
    val kittens = Organizations.typelevel %% "kittens" % Versions.kittens

    // Http4s
    val http4sDsl                = Organizations.http4s %% "http4s-dsl"                % Versions.http4s
    val http4sEmberServer        = Organizations.http4s %% "http4s-ember-server"       % Versions.http4s
    val http4sPrometheusMetrics  = Organizations.http4s %% "http4s-prometheus-metrics" % Versions.http4sPrometheus

    // Circe
    val circeGeneric = Organizations.circe %% "circe-generic" % Versions.circe
    val circeParser = Organizations.circe %% "circe-parser" % Versions.circe

    // Ciris
    val ciris = Organizations.ciris %% "ciris" % Versions.ciris

    // Skunk
    val skunkCore = Organizations.tpolecat %% "skunk-core" % Versions.skunk

    // Kafka
    val fs2Kafka = Organizations.fd4s %% "fs2-kafka" % Versions.fs2Kafka

    // Scodec
    val scodecCore = Organizations.scodec %% "scodec-core" % Versions.scodec

    // Iron Types
    val iron       = Organizations.iltotore %% "iron"        % Versions.iron
    val ironCats   = Organizations.iltotore %% "iron-cats"   % Versions.iron
    val ironCirce  = Organizations.iltotore %% "iron-circe"  % Versions.iron
    val ironSkunk  = Organizations.iltotore %% "iron-skunk"  % Versions.iron
    val ironScodec = Organizations.iltotore %% "iron-scodec" % Versions.iron

    // Ducktape
    val ducktape = Organizations.arainko %% "ducktape" % Versions.ducktape

    // Java
    val logbackClassic = Organizations.logback % "logback-classic" % Versions.logback
  }
}

