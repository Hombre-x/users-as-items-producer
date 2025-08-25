package com.mycode
package http.middleware

import cats.syntax.all.*
import cats.data.*
import cats.data.Validated
import cats.effect.Async
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*

import typeclasses.Validator
import domain.validation.ValidationError

object ValidationMiddleware:

  /** Decode the request body as JSON of type `A`, validate it using an implicit Validator[A], and on success, delegate
    * to the provided routes builder.
    *
    * Usage: wrap routes that expect a JSON body of type `A`. Only apply this to routes that actually receive such a
    * body (e.g., POST/PUT endpoints).
    */
  def validateBody[F[_]: {Async}, A: Validator](
      routes: A => HttpRoutes[F]
  )(using EntityDecoder[F, A]): HttpRoutes[F] = Kleisli { (req: Request[F]) =>
    val v = summon[Validator[A]]
    // Decode the JSON body then validate it. On failure, return 400.
    OptionT.liftF(req.as[A].attempt).flatMap {
      case Right(decoded: A) =>
        v.validate(decoded) match
          case Validated.Valid(valid)    =>
            routes(valid).run(req)
          case Validated.Invalid(errors) =>
            OptionT.pure[F](
              Response(Status.BadRequest).withEntity(
                ValidationError("Validation failed", errors.toNonEmptyList)
              )
            )
      case Left(_)           =>
        OptionT.pure[F](
          Response(Status.BadRequest).withEntity(
            ValidationError("Invalid JSON", NonEmptyList.one("Malformed or missing JSON body"))
          )
        )
    }
  }

end ValidationMiddleware
