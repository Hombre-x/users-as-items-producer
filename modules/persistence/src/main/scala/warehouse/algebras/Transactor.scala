package warehouse.algebras

import cats.effect.MonadCancelThrow
import skunk.Session
import warehouse.domain.skunk.Pool

trait Transactor[F[_]]:
  def transact[A](f: Session[F] => F[A]): F[A]

object Transactor:
  def pooledSession[F[_]: {MonadCancelThrow}](postgres: Pool[F]): Transactor[F] =
    new Transactor[F]:
      override def transact[A](f: Session[F] => F[A]): F[A] =
        postgres.use: session =>
          session.transaction.use: _ =>
            f(session)
