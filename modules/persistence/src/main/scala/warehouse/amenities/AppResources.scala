package warehouse.amenities

import cats.effect.std.Console
import cats.effect.{Async, Resource}
import cats.syntax.all.*
import fs2.io.net.Network
import org.typelevel.log4cats.Logger
import org.typelevel.otel4s.trace.Tracer
import skunk.codec.text.*
import skunk.syntax.all.*
import skunk.{Session, SessionPool}
import warehouse.config.Config.AppConfig
import warehouse.domain.skunk.Pool

case class AppResources[F[_]](
    postgres: Pool[F]
)

object AppResources:
  def make[F[_]: {Async, Tracer, Network, Logger, Console}](config: AppConfig): Resource[F, AppResources[F]] =

    def checkPostgresConnection(postgres: Pool[F]): F[Unit] = postgres.use: session =>
      session
        .unique(sql"select version();".query(text))
        .flatMap: v =>
          Logger[F].info(s"Connected to PostgreSQL $v")

    def mkPostgres: SessionPool[F] =
      Session
        .pooled[F](
          host = config.postgresConfig.host,
          port = config.postgresConfig.port,
          user = config.postgresConfig.user,
          password = Some(config.postgresConfig.password.value),
          database = config.postgresConfig.database,
          max = 8
        )
        .evalTap(checkPostgresConnection)

    mkPostgres.map(postgres => AppResources(postgres))
