package com.mycode
package amenities

import cats.effect.{Resource, Sync}
import domain.config.ProducerConfig

import fs2.kafka.ProducerSettings

import java.util.UUID

trait MkKafkaProducerSettings[F[_]]:

  def create: Resource[F, ProducerSettings[F, UUID, String]]

end MkKafkaProducerSettings

object MkKafkaProducerSettings:
  def make[F[_]: Sync](config: ProducerConfig): MkKafkaProducerSettings[F] = new MkKafkaProducerSettings[F]:
    override def create: Resource[F, ProducerSettings[F, UUID, String]] =
      Resource.pure(
        ProducerSettings[F, UUID, String]
          .withBootstrapServers(config.bootstrapServer)
      )
