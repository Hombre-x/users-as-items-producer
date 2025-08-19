package com.mycode
package validation

import cats.syntax.all.*
import cats.instances.list.*
import cats.data.{Validated, ValidatedNel}
import io.github.iltotore.iron.*
import io.github.iltotore.iron.cats.*
import io.github.iltotore.iron.constraint.all.*
import domain.user.*

object UserValidation:
  
  def validateUsername(username: String): ValidatedNel[String, Username] =
    Username.validatedNel(username)
    
  def validateCreateUser(user: CreateUser): ValidatedNel[String, CreateUser] =
    (
      Username.validatedNel(user.username.value),
      Email.validatedNel(user.email.value),
      Name.validatedNel(user.name.value)
    ).mapN(CreateUser.apply)
    
 
end UserValidation
