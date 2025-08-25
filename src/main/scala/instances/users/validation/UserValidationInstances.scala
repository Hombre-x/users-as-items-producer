package com.mycode
package instances.users.validation

import cats.data.ValidatedNec

import typeclasses.Validator
import domain.user.*
import validation.UserValidation

given createUserValidation: Validator[CreateUser] with
  def validate(input: CreateUser): ValidatedNec[String, CreateUser] =
    UserValidation.validateCreateUser(input)
