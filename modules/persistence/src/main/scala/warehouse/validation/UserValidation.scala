package warehouse.validation

import cats.data.{Validated, ValidatedNec}
import cats.syntax.all.*
import io.github.iltotore.iron.cats.*
import warehouse.domain.user.{CreateUser, Email, Name, UpdateUser, Username}

object UserValidation:

  def validateUsername(username: String): ValidatedNec[String, Username] =
    Username.validatedNec(username)

  def validateCreateUser(user: CreateUser): ValidatedNec[String, CreateUser] =
    (
      Username.validatedNec(user.username.value),
      Email.validatedNec(user.email.value),
      Name.validatedNec(user.name.value)
    ).mapN(CreateUser.apply)

  def validateUpdateUser(user: UpdateUser): ValidatedNec[String, UpdateUser] =
    (
      Username.validatedNec(user.username.value),
      Email.validatedNec(user.email.value),
      Name.validatedNec(user.name.value)
    ).mapN(UpdateUser.apply)

end UserValidation
