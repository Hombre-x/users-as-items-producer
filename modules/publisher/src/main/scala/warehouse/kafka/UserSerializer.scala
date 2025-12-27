package warehouse.kafka

import cats.effect.Sync
import cats.syntax.all.*
import fs2.kafka.Serializer
import warehouse.command.given
import io.circe.syntax.*
import warehouse.domain.commands.UserCommand

object UserSerializer:
  def serializer[F[_]: Sync]: Serializer[F, UserCommand] =
    Serializer.lift(_.asJson.noSpaces.getBytes("UTF-8").pure[F])
