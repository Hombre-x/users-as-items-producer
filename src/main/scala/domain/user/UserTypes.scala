package com.mycode
package domain.user

import io.circe.Codec
import io.github.iltotore.iron.*
import io.github.iltotore.iron.circe.given
import io.github.iltotore.iron.constraint.char.Letter
import io.github.iltotore.iron.constraint.collection.ForAll
import io.github.iltotore.iron.constraint.string.{Alphanumeric, Match}

import java.util.UUID

type UserId = UUID

type Username = Username.T
object Username extends RefinedType[String, Alphanumeric]

type ValidEmail = DescribedAs[Match["^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$"], "Invalid email format"]

type Email = Email.T
object Email extends RefinedType[String, ValidEmail]

type Name = Name.T
object Name extends RefinedType[String, ForAll[Letter]]

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
