package com.mycode
package config

import cats.syntax.all.*
import cats.effect.Async
import domain.config.{AppConfig, ConsumerConfig, HttpConfig, ProducerConfig}

import ciris.*
import com.comcast.ip4s.Port

object Config:

  def load[F[_]: Async]: F[AppConfig] =
    (
      env("KAFKA_BOOTSTRAP_SERVER").default("localhost:19092"),
      env("WAREHOUSE_SERVER_PORT").as[Int].default(9000)
    )
      .parMapN((kafkaUri, port) =>
        AppConfig(
          producerConfig = ProducerConfig(
            kafkaUri
          ),
          consumerConfig = ConsumerConfig(
            kafkaUri,
            "default-group"
          ),
          httpConfig = HttpConfig(
            "localhost",
            Port.fromInt(port).get
          )
        )
      )
      .load[F]

end Config
