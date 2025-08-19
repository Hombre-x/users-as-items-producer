package com.mycode
package domain.skunk

import skunk.Codec
import skunk.codec.all.*

import io.github.iltotore.iron.*
import io.github.iltotore.iron.constraint.all.*
import io.github.iltotore.iron.skunk.*

import domain.user.*

object UserCodecs:

  val userId: Codec[UserId] = uuid
  val username: Codec[Username] = varchar.refined[Alphanumeric].imap(Username.apply)(_.value)
  val email: Codec[Email] = varchar.refined[ValidEmail].imap(Email.apply)(_.value)
  val name: Codec[Name] = varchar.refined[ForAll[Letter]].imap(Name.apply)(_.value)

end UserCodecs
