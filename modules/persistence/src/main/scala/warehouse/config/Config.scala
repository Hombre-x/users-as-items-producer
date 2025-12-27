package warehouse.config

import cats.effect.Async
import cats.syntax.all.*
import ciris.*
import com.comcast.ip4s.Port
import warehouse.domain.config.*

object Config:

  case class AppConfig(producerConfig: ProducerConfig, 
                       consumerConfig: ConsumerConfig, 
                       postgresConfig: PostgreSQLConfig, 
                       httpConfig: HttpConfig)

  def load[F[_]: Async]: F[AppConfig] =
    (
      env("KAFKA_BOOTSTRAP_SERVER").default("localhost:19092"),
      env("WAREHOUSE_SERVER_PORT").as[Int].default(9000),
      env("WAREHOUSE_DATABASE_HOST").default("localhost"),
      env("WAREHOUSE_DATABASE_PORT").as[Int].default(15432),
      env("WAREHOUSE_DATABASE_USER").default("postgres"),
      env("WAREHOUSE_DATABASE_PASSWORD").secret
    )
      .parMapN((kafkaUri, port, dbHost, dbPort, dbUser, dbPassword) =>
        AppConfig(
          producerConfig = ProducerConfig(
            kafkaUri
          ),
          consumerConfig = ConsumerConfig(
            kafkaUri,
            "default-group"
          ),
          postgresConfig = PostgreSQLConfig(
            dbHost,
            dbPort,
            dbUser,
            dbPassword,
            "warehouse-db"
          ),
          httpConfig = HttpConfig(
            "localhost",
            Port.fromInt(port).get
          )
        )
      )
      .load[F]

end Config
