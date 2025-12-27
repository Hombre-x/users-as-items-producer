package warehouse.algebras

import cats.effect.std.UUIDGen
import cats.effect.{Async, Resource}
import cats.syntax.all.*
import fs2.kafka.{KafkaProducer, ProducerSettings}

import java.util.UUID

trait Producer[F[_], A]:

  def send(a: A): F[Unit]

end Producer

object Producer:

  def kafka[F[_]: Async, A](
      settings: ProducerSettings[F, UUID, A],
      topic: String
  ): Resource[F, Producer[F, A]] =
    KafkaProducer
      .resource(settings)
      .map: p =>
        new:
          def send(a: A): F[Unit] =
            for
              key <- UUIDGen.randomUUID[F]
              _   <- p.produceOne_(topic, key, a).flatten.void
            yield ()

end Producer
