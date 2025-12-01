package com.mycode
package serializers.kafka

import cats.syntax.all.*
import cats.effect.Sync
import io.circe.syntax.*
import fs2.kafka.Serializer
import domain.commands.UserCommand

object UserCommandCodec:
  def serializer[F[_]: Sync]: Serializer[F, UserCommand] =
    Serializer.lift: command =>
      command.asJson.noSpaces.getBytes("UTF-8").pure[F]

end UserCommandCodec
