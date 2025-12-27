package warehouse.core

import cats.ApplicativeThrow
import cats.effect.std.UUIDGen
import cats.effect.{Clock, Concurrent}
import cats.syntax.all.*
import io.github.arainko.ducktape.*
import warehouse.algebras.Users
import warehouse.domain.user.{CreateUser, UpdateUser, User, UserId, UserNotFound, Username}

import java.time.Instant
import java.util.UUID

class Warehouse[F[_]: {ApplicativeThrow, UUIDGen, Clock, Concurrent}](users: Users[F]):

  def addUser(createUser: CreateUser): F[Username] =
    for
      uuid     <- UUIDGen.randomUUID[F]
      user      = createUser.into[User].transform(Field.const[CreateUser, User, UserId, UUID](_.id, uuid))
      username <- users.create(user)
      now      <- Clock[F].realTime
//      command   = UserCommand.CreateUserCommand(uuid, Instant.ofEpochMilli(now.toMillis), createUser)
//      _        <- producer.send(command)
    yield username

  def updateUser(updateUser: UpdateUser): F[Username] =
    for
      userOpt  <- users.get(updateUser.username)
      username <- userOpt match
                    case Some(existingUser) =>
                      for
                        now      <- Clock[F].realTime
//                        command   =
//                          UserCommand.UpdateUserCommand(existingUser.id, Instant.ofEpochMilli(now.toMillis), updateUser)
                        username <- users.update(updateUser)
//                        _        <- producer.send(command)
                      yield username
                    case None               => UserNotFound(updateUser.username).raiseError[F, Username]
    yield username

  def deleteUser(username: Username): F[Boolean] =
    for
      userOpt    <- users.get(username)
      wasDeleted <- userOpt match
                      case Some(user) =>
                        for
                          now     <- Clock[F].realTime
//                          command  = UserCommand.DeleteUserCommand(user.id, Instant.ofEpochMilli(now.toMillis), username)
                          deleted <- users.delete(username)
//                          _       <- producer.send(command)
                        yield deleted
                      case None       => false.pure[F]
    yield wasDeleted

end Warehouse

object Warehouse:

  def apply[F[_]: {ApplicativeThrow, UUIDGen, Clock, Concurrent}](
      users: Users[F],
  ): Warehouse[F] = new Warehouse(users) 

end Warehouse
