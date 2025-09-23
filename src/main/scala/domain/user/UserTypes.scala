package com.mycode
package domain.user

import io.circe.Codec
import io.github.iltotore.iron.*
import io.github.iltotore.iron.circe.given
import io.github.iltotore.iron.constraint.collection.ForAll
import io.github.iltotore.iron.constraint.string.{Alphanumeric, Match, ValidEmail}

import java.util.UUID
import scala.util.control.NoStackTrace

type UserId = UUID

type Username = Username.T
object Username extends RefinedType[String, Alphanumeric]

type Email = Email.T
object Email extends RefinedType[String, ValidEmail]

type ValidName = DescribedAs[Match["^[a-zA-Z]+( [a-zA-Z]+)*$"], "Name must contain only letters and spaces"]

type Name = Name.T
object Name extends RefinedType[String, ValidName]

case class CreateUser(
    username: Username,
    email: Email,
    name: Name
) derives Codec

case class UpdateUser(
    username: Username,
    email: Email,
    name: Name
) derives Codec

case class User(
    id: UserId,
    username: Username,
    email: Email,
    name: Name
) derives Codec

case class UserNotFound(username: Username)          extends NoStackTrace
case class UsernameAlreadyExists(username: Username) extends NoStackTrace
