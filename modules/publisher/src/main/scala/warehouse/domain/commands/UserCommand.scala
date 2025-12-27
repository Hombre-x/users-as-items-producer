package warehouse.domain.commands

import io.circe.Codec
import warehouse.domain.user.{CreateUser, UpdateUser, Username}

import java.time.Instant

enum UserCommand:

  def id: CommandId
  def createdAt: Instant

  case CreateUserCommand(
      id: CommandId,
      createdAt: Instant,
      username: Username
  )

  case DeleteUserCommand(
      id: CommandId,
      createdAt: Instant,
      username: Username
  )

  case UpdateUserCommand(
      id: CommandId,
      createdAt: Instant,
      username: Username
  )

end UserCommand
