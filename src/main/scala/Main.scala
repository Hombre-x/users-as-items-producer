package com.mycode

import cats.effect.{IO, IOApp}

import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger

import amenities.{AppResources, MkHttpServer}

import config.Config

import com.mycode.http.HttpApi

object Main extends IOApp.Simple:

  given logger: Logger[IO] = Slf4jLogger.getLoggerFromName("Main")

  override def run: IO[Unit] =

    Config
      .load[IO]
      .flatMap: config =>
        logger.info(s"Loading config for application: $config") >>
          AppResources
            .make(config)
            .use: _ =>
              val httpApi = HttpApi.make[IO]
              val server  = MkHttpServer.make[IO].ember(config.httpConfig.port, httpApi.routes)

              server.useForever
                .onCancel(logger.info("Closing server..."))

  end run

end Main
