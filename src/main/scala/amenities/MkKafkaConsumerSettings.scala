package com.mycode
package amenities

import cats.effect.{Resource, Sync}
import domain.config.ConsumerConfig

import fs2.kafka.{AutoOffsetReset, ConsumerSettings}

import java.util.UUID

trait MkKafkaConsumerSettings[F[_]]:

  def create: Resource[F, ConsumerSettings[F, UUID, String]]

end MkKafkaConsumerSettings

object MkKafkaConsumerSettings:
  def make[F[_]: Sync](config: ConsumerConfig): MkKafkaConsumerSettings[F] = new MkKafkaConsumerSettings[F]:
    override def create: Resource[F, ConsumerSettings[F, UUID, String]] =
      Resource.pure(
        ConsumerSettings[F, UUID, String]
          .withAutoOffsetReset(AutoOffsetReset.Earliest)
          .withBootstrapServers(config.bootstrapServer)
          .withGroupId(config.groupId)
      )
