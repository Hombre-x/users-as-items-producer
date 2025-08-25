package com.mycode
package algebras

import cats.effect.{MonadCancelThrow, Sync}
import cats.syntax.all.*

import skunk.*
import skunk.data.Completion.Delete
import skunk.codec.temporal.timestamp
import skunk.syntax.all.*

import java.time.LocalDateTime

import domain.user.*
import domain.skunk.Pool
import domain.skunk.UserCodecs.*

trait Users[F[_]]:

  def get(username: Username): F[Option[User]]
  def getById(userId: UserId): F[Option[User]]
  def create(user: CreateUser): F[Username]
  def update(user: UpdateUser): F[Username]
  def delete(username: Username): F[Boolean]

end Users

object Users:

  def postgres[F[_]: {Sync, MonadCancelThrow}](postgres: Pool[F]): Users[F] = new Users[F]:

    import UsersSql.{selectUser, selectUserById, createUser, changeUser, deleteUser}

    override def get(username: Username): F[Option[User]] = postgres.use(se => se.option(selectUser)(username))

    override def getById(userId: UserId): F[Option[User]] = postgres.use(se => se.option(selectUserById)(userId))

    override def create(user: CreateUser): F[Username] =
      postgres.use: se =>
        se.execute(createUser)(user)
          .as(user.username)
          .recoverWith:
            case e => e.raiseError[F, Username]

    override def update(user: UpdateUser): F[Username] =
      postgres.use: se =>
        for
          now      <- Sync[F].delay(LocalDateTime.now())
          cmd      <- se.prepare(changeUser)
          username <- cmd
                        .execute((user.email, user.name, now, user.username))
                        .as(user.username)
                        .recoverWith:
                          case e => e.raiseError[F, Username]
        yield username

    override def delete(username: Username): F[Boolean] =
      postgres.use: se =>
        se.execute(deleteUser)(username)
          .map:
            case Delete(n) if n > 0 => true
            case _                  => false
          .recoverWith:
            case e => e.raiseError[F, Boolean]

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

  private val createUserSql: Fragment[CreateUser] =
    sql"""
      INSERT INTO users (username, email, "name")
      VALUES ($username, $email, $name);
    """.to[CreateUser]

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
  val selectUser     = selectUserSql.query(userDecoder)
  val selectUserById = selectUserByIdSql.query(userDecoder)
  val createUser     = createUserSql.command
  val changeUser     = changeUserSql.command
  val deleteUser     = deleteUserSql.command

end UsersSql
