package warehouse.algebras

import cats.effect.Concurrent
import fs2.Stream
import io.circe.Codec
import io.circe.parser.decode
import skunk.data.Identifier
import warehouse.domain.skunk.Pool

trait Poller[F[_], A]:

  def receive(channel: Identifier): Stream[F, A]

end Poller

object Poller:
  def skunk[F[_] : Concurrent, A : Codec](postgres: Pool[F]): Poller[F, A] =
    new:
      def receive(channel: Identifier): Stream[F, A] =
        Stream
          .resource(postgres)
          .flatMap: session =>
            session.channel(channel).listen(1000)
          .map: notification =>
            decode[A](notification.value)
          .rethrow

  def string[F[_] : Concurrent](postgres: Pool[F]): Poller[F, String] =
    new:
      def receive(channel: Identifier): Stream[F, String] =
        Stream
          .resource(postgres)
          .flatMap: session =>
            session.channel(channel).listen(1000)
          .map: notification =>
            notification.value
