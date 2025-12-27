package warehouse.validation.syntax

import cats.MonadThrow
import cats.data.Validated
import cats.syntax.all.*
import warehouse.domain.validation.ValidationError
import warehouse.typeclasses.Validator

extension [A](v: A)
  def validateOrFail[F[_] : MonadThrow](using validator: Validator[A]): F[A] =
    validator.validate(v) match
      case Validated.Valid(value)    => value.pure[F]
      case Validated.Invalid(errors) =>
        ValidationError("Validation failed", errors.toNonEmptyList).raiseError[F, A]
