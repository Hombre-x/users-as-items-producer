package warehouse.algebras

import cats.effect.MonadCancelThrow
import cats.syntax.all.*
import skunk.data.Identifier
import warehouse.domain.skunk.Pool

import java.util.UUID

trait Notifier[F[_]]:
  def notify(channel: Identifier)(notificationId: UUID): F[Unit]

object Notifier:
  def skunk[F[_] : MonadCancelThrow](postgres: Pool[F]): Notifier[F] = new Notifier[F]:
    def notify(channel: Identifier)(notificationId: UUID): F[Unit] =
      postgres.use: session =>
        for
          ch <- session.channel(channel).pure[F]
          _  <- ch.notify(notificationId.toString)
        yield ()
