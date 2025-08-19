package com.mycode
package algebras

import cats.syntax.all.*
import cats.effect.std.UUIDGen
import cats.effect.{Async, Resource}
import fs2.kafka.{KafkaProducer, ProducerSettings}

trait Producer[F[_], A]:
  
  def send(a: A): F[Unit]
  
end Producer

object Producer:
  
  def kafka[F[_] : Async, A](
      settings: ProducerSettings[F, String, A],
      topic: String
  ): Resource[F, Producer[F, A]] =
    KafkaProducer.resource(settings).map: p =>
      new:
        def send(a: A): F[Unit] =
          for
            key <- UUIDGen.randomUUID[F].map(_.toString)
            _ <- p.produceOne_(topic, key, a).flatten.void
          yield ()

end Producer
