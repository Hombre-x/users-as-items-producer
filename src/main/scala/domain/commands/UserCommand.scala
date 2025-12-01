package com.mycode
package domain.commands

import domain.user.*

import io.circe.Codec
import io.circe.derivation.{Configuration, ConfiguredCodec}
import io.github.iltotore.iron.circe.given

import java.time.Instant

enum UserCommand:

  def id: CommandId
  def createdAt: Instant

  case CreateUserCommand(
      id: CommandId,
      createdAt: Instant,
      user: CreateUser
  )

  case DeleteUserCommand(
      id: CommandId,
      createdAt: Instant,
      username: Username
  )

  case UpdateUserCommand(
      id: CommandId,
      createdAt: Instant,
      user: UpdateUser
  )

end UserCommand

object UserCommand:

  given Configuration      = Configuration.default.withDiscriminator("$type")
  given Codec[UserCommand] = ConfiguredCodec.derived

end UserCommand
