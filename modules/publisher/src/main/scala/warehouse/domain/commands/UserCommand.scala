package warehouse.domain.commands

import warehouse.domain.user.Username

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
