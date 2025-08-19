package com.mycode
package domain.commands

import java.time.Instant

enum UserCommand:
  
  def id: CommandId
  def createdAt: Instant
  
  case CreateUser(
    id: CommandId,
    createdAt: Instant,
    name: String,
    email: String
  )
    
end UserCommand
