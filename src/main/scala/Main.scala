package com.mycode

import algebras.Users
import amenities.{AppResources, MkHttpServer}
import config.Config
import core.Warehouse
import http.HttpApi

import cats.effect.{IO, IOApp}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.otel4s.trace.Tracer

object Main extends IOApp.Simple:

  given logger: Logger[IO] = Slf4jLogger.getLoggerFromName("Main")
  given tracer: Tracer[IO] = Tracer.noop[IO]

  override def run: IO[Unit] =

    Config
      .load[IO]
      .flatMap: config =>
        logger.info(s"Loading config for application: $config") >>
          AppResources
            .make[IO](config)
            .use: resources =>
              val users     = Users.postgres(resources.postgres)
              val warehouse = Warehouse[IO](users, resources.userProducer)
              val httpApi   = HttpApi.make[IO](warehouse)
              val server    = MkHttpServer.make[IO].ember(config.httpConfig.port, httpApi.routes)

              server.useForever
                .onCancel(logger.info("Closing server..."))

  end run

end Main
