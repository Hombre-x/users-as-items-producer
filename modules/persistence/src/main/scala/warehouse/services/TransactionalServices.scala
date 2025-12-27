package warehouse.services

import cats.effect.kernel.{Resource, Sync}
import warehouse.algebras.{Notifier, Outbox, Users}
import warehouse.domain.skunk.Pool

object TransactionalServices:

  def make[F[_] : Sync](postgres: Pool[F]): Resource[F, TransactionalServices[F]] =
    postgres.map: se =>
      val _outbox   = Outbox.postgresSession[F](se)
      val _notifier = Notifier.skunk[F](se)
      new TransactionalServices[F](
        users = Users.fromSession[F](_notifier, _outbox, se),
        outbox = _outbox,
        notifier = _notifier
      ) {}

end TransactionalServices

sealed abstract class TransactionalServices[F[_]] private (
    val users: Users[F],
    val outbox: Outbox[F],
    val notifier: Notifier[F]
)
