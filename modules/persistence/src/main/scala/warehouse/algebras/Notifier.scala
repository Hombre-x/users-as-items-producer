package warehouse.algebras

import cats.Monad

import cats.syntax.all.*
import skunk.Session
import skunk.data.Identifier


import java.util.UUID

trait Notifier[F[_]]:
  def notify(channel: Identifier)(notificationId: UUID): F[Unit]

object Notifier:
  def skunk[F[_] : Monad](session: Session[F]): Notifier[F] = new Notifier[F]:
    def notify(channel: Identifier)(notificationId: UUID): F[Unit] =
      for
        ch <- session.channel(channel).pure[F]
        _  <- ch.notify(notificationId.toString)
      yield ()

