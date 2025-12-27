package warehouse

import cats.effect.{IO, IOApp}
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import org.typelevel.otel4s.trace.Tracer
import warehouse.algebras.{Outbox, Poller}
import warehouse.amenities.AppResources
import warehouse.config.Config
import warehouse.core.Publisher

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
              val poller    = Poller.string(resources.postgres)
              val outbox    = Outbox.postgresPool(resources.postgres)
              val publisher = Publisher(resources.userProducer, poller, outbox)

              publisher.stream
                .onFinalize(logger.info("Closing server..."))
                .compile
                .drain
end Main
