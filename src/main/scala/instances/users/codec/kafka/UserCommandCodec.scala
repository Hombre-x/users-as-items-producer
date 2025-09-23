package com.mycode
package instances.users.codec.kafka

import cats.syntax.all.*
import cats.effect.Sync
import scodec.Codec
import fs2.kafka.Serializer
import io.github.iltotore.iron.scodec.given
import domain.user.*
import domain.commands.UserCommand
import instances.users.codec.given
import instances.time.codec.given

object UserCommandCodec:

  given Codec[UserCommand] = Codec.derived

  def serializer[F[_]: Sync as io]: Serializer[F, UserCommand] =
    Serializer.lift: command =>
      io.fromEither:
        Codec[UserCommand]
          .encode(command)
          .toEither
          .bimap(
            err => new Exception(err.messageWithContext),
            _.toByteArray,
          )


end UserCommandCodec
