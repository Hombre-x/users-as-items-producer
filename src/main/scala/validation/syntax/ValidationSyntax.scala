package com.mycode
package validation.syntax

import cats.data.Validated
import cats.syntax.all.*
import typeclasses.Validator
import domain.validation.ValidationError
import cats.MonadThrow

extension [A](v: A)
  def validateOrFail[F[_]: MonadThrow](using validator: Validator[A]): F[A] =
    validator.validate(v) match
      case Validated.Valid(value)    => value.pure[F]
      case Validated.Invalid(errors) =>
        ValidationError("Validation failed", errors.toNonEmptyList).raiseError[F, A]
