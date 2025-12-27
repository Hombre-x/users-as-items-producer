package warehouse.instances.users.validation

import cats.data.ValidatedNec
import warehouse.domain.user.CreateUser
import warehouse.typeclasses.Validator
import warehouse.validation.UserValidation

given createUserValidation: Validator[CreateUser] with
  def validate(input: CreateUser): ValidatedNec[String, CreateUser] =
    UserValidation.validateCreateUser(input)
