package warehouse.config

import cats.effect.Async
import cats.syntax.all.*
import ciris.*
import warehouse.domain.config.*

object Config:

  case class AppConfig(producerConfig: ProducerConfig, postgresConfig: PostgreSQLConfig)

  def load[F[_]: Async]: F[AppConfig] =
    (
      env("KAFKA_BOOTSTRAP_SERVER").default("localhost:19092"),
      env("WAREHOUSE_SERVER_PORT").as[Int].default(9000),
      env("WAREHOUSE_DATABASE_HOST").default("localhost"),
      env("WAREHOUSE_DATABASE_PORT").as[Int].default(15432),
      env("WAREHOUSE_DATABASE_USER").default("postgres"),
      env("WAREHOUSE_DATABASE_PASSWORD").secret
    )
      .parMapN((kafkaUri, _, dbHost, dbPort, dbUser, dbPassword) =>
        AppConfig(
          producerConfig = ProducerConfig(
            kafkaUri
          ),
          postgresConfig = PostgreSQLConfig(
            dbHost,
            dbPort,
            dbUser,
            dbPassword,
            "warehouse-db"
          )
        )
      )
      .load[F]

end Config
