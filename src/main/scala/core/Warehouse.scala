package com.mycode
package core

import cats.ApplicativeThrow
import cats.syntax.all.*
import cats.effect.std.UUIDGen
import cats.effect.Concurrent
import io.github.arainko.ducktape.*
import algebras.{Producer, Users}
import domain.user.{CreateUser, UpdateUser, User, UserId, UserNotFound, Username}

import cats.effect.Clock
import domain.commands.UserCommand

import java.time.Instant
import java.util.UUID

class Warehouse[F[_]: {ApplicativeThrow, UUIDGen, Clock, Concurrent}](
    users: Users[F],
    producer: Producer[F, UserCommand]
):

  def addUser(createUser: CreateUser): F[Username] =
    for
      uuid     <- UUIDGen.randomUUID[F]
      user      = createUser.into[User].transform(Field.const[CreateUser, User, UserId, UUID](_.id, uuid))
      username <- users.create(user)
      now      <- Clock[F].realTime
      command   = UserCommand.CreateUserCommand(uuid, Instant.ofEpochMilli(now.toMillis), createUser)
      _        <- producer.send(command)
    yield username

  def updateUser(updateUser: UpdateUser): F[Username] =
    for
      userOpt  <- users.get(updateUser.username)
      username <- userOpt match
                    case Some(existingUser) =>
                      for
                        now      <- Clock[F].realTime
                        command   =
                          UserCommand.UpdateUserCommand(existingUser.id, Instant.ofEpochMilli(now.toMillis), updateUser)
                        username <- users.update(updateUser)
                        _        <- producer.send(command)
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
                          command  = UserCommand.DeleteUserCommand(user.id, Instant.ofEpochMilli(now.toMillis), username)
                          deleted <- users.delete(username)
                          _       <- producer.send(command)
                        yield deleted
                      case None       => false.pure[F]
    yield wasDeleted

end Warehouse

object Warehouse:

  def apply[F[_]: {ApplicativeThrow, UUIDGen, Clock, Concurrent}](
      users: Users[F],
      producer: Producer[F, UserCommand]
  ): Warehouse[F] = new Warehouse(users, producer)

end Warehouse
