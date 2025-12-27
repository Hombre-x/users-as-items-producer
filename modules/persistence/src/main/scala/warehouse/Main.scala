package warehouse

import cats.effect.{IO, IOApp}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.otel4s.trace.Tracer
import warehouse.algebras.*
import warehouse.amenities.{AppResources, MkHttpServer}
import warehouse.config.Config
import warehouse.core.Warehouse
import warehouse.http.HttpApi

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
              val txn       = Transactor.pooledSession(resources.postgres)
              val notifier  = Notifier.skunk(resources.postgres)
              val outbox    = Outbox.postgres(resources.postgres)
              val users     = Users.postgres(txn, notifier, outbox, resources.postgres)
              val warehouse = Warehouse[IO](users)
              val httpApi   = HttpApi.make[IO](warehouse)
              val server    = MkHttpServer.make[IO].ember(config.httpConfig.port, httpApi.routes)

              server.useForever
                .onCancel(logger.info("Closing server..."))

  end run

end Main
