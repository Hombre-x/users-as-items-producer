package com.mycode
package config

import cats.effect.Async
import domain.config.{AppConfig, ConsumerConfig, ProducerConfig}

import ciris.*

object Config:

  def load[F[_]: Async]: F[AppConfig] =
    (
      env("KAFKA_BOOTSTRAP_SERVER")
        .default("localhost:9092")
      )
      .map(kafkaUri =>
        AppConfig(
          producerConfig = ProducerConfig(
            kafkaUri
          ),
          consumerConfig = ConsumerConfig(
            kafkaUri,
            "default-group"
          )
        )
      )
      .load[F]

end Config
