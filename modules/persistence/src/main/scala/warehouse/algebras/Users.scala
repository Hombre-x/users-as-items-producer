package warehouse.algebras

import cats.effect.std.UUIDGen
import cats.effect.{MonadCancelThrow, Sync}
import cats.syntax.all.*
import skunk.*
import skunk.codec.temporal.timestamp
import skunk.data.Completion.Delete
import skunk.syntax.all.*
import warehouse.domain.EventType
import warehouse.domain.outbox.CreateOutboxEntry

import warehouse.domain.skunk.UserCodecs.*
import warehouse.domain.user.*

import java.time.LocalDateTime

trait Users[F[_]]:

  def get(username: Username): F[Option[User]]
  def getById(userId: UserId): F[Option[User]]
  def create(user: User): F[Username]
  def update(user: UpdateUser): F[Username]
  def delete(username: Username): F[Boolean]

end Users

object Users:

  def fromSession[F[_] : {Sync, MonadCancelThrow}](
      notifier: Notifier[F],
      outbox: Outbox[F],
      session: Session[F]
  ): Users[F] = new Users[F]:

    import UsersSql.*

    override def get(username: Username): F[Option[User]] = session.option(selectUser)(username)

    override def getById(userId: UserId): F[Option[User]] = session.option(selectUserById)(userId)

    override def create(user: User): F[Username] =
      session.transaction.surround:
        for
          eventId  <- UUIDGen[F].randomUUID
          username <- session
                        .execute(createUser)(user)
                        .as(user.username)
                        .recoverWith:
                          case SqlState.UniqueViolation(_) =>
                            UsernameAlreadyExists(user.username).raiseError[F, Username]

          entry     = CreateOutboxEntry(eventId, EventType.UserCreated, user.username)
          outboxId <- outbox.persist(entry)
          _        <- notifier.notify(id"warehouse_outbox_creations")(outboxId)
        yield username

    override def update(user: UpdateUser): F[Username] =
      session.transaction.use: xa =>
        for
          now       <- Sync[F].delay(LocalDateTime.now())
          eventId   <- UUIDGen[F].randomUUID
          savepoint <- xa.savepoint
          cmd       <- session.prepare(changeUser)
          username  <- cmd
                         .execute((user.email, user.name, now, user.username))
                         .as(user.username)
                         .recoverWith:
                           case ex => xa.rollback(savepoint) >> ex.raiseError[F, Username]

          entry     = CreateOutboxEntry(eventId, EventType.UserUpdated, user.username)
          outboxId <- outbox.persist(entry)
          _        <- notifier.notify(id"warehouse_outbox_updates")(outboxId)
        yield username

    override def delete(username: Username): F[Boolean] =
      session.transaction.surround:
        for
          eventId <- UUIDGen[F].randomUUID
          deleted <- session
                       .execute(deleteUser)(username)
                       .map:
                         case Delete(n) if n > 0 => true
                         case _                  => false

          entry     = CreateOutboxEntry(eventId, EventType.UserDeleted, username)
          outboxId <- outbox.persist(entry)
          _        <- notifier.notify(id"warehouse_outbox_deletions")(outboxId)
        yield deleted
end Users

private object UsersSql:

  // Encoders & Decoders
  val createUserEncoder: Encoder[CreateUser] =
    (username *: email *: name).to[CreateUser]

  val updateUserEncoder: Encoder[UpdateUser] =
    (username *: email *: name).to[UpdateUser]

  val userDecoder: Decoder[User] =
    (userId ~ username ~ email ~ name).map:
      case u ~ n ~ e ~ na => User(u, n, e, na)

  // Scripts
  private val selectUserSql: Fragment[Username] =
    sql"""
      SELECT id, username, email, "name"
      FROM users
      WHERE username = $username;
    """

  private val selectUserByIdSql: Fragment[UserId] =
    sql"""
        SELECT id, username, email, "name"
        FROM users
        WHERE id = $userId;
    """

  private val createUserSql: Fragment[User] =
    sql"""
      INSERT INTO users (id, username, email, "name")
      VALUES ($userId, $username, $email, $name);
    """.to[User]

  private val changeUserSql: Fragment[Email *: Name *: LocalDateTime *: Username *: EmptyTuple] =
    sql"""
      UPDATE users
      SET email = $email, "name" = $name, updated_at = $timestamp
      WHERE username = $username;
    """

  private val deleteUserSql: Fragment[Username] =
    sql"""
      DELETE FROM users
      WHERE username = $username;
    """

  // Queries & Commands
  val selectUser: Query[Username, User]                           = selectUserSql.query(userDecoder)
  val selectUserById: Query[UserId, User]                         = selectUserByIdSql.query(userDecoder)
  val createUser: Command[User]                                   = createUserSql.command
  val changeUser: Command[(Email, Name, LocalDateTime, Username)] = changeUserSql.command
  val deleteUser: Command[Username]                               = deleteUserSql.command

end UsersSql
