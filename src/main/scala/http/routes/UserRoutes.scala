package com.mycode
package http.routes

import cats.Monad
import cats.effect.{Concurrent, MonadCancelThrow, Sync}
import cats.syntax.all.*
import algebras.Users
import domain.user.*
import io.circe.syntax.*
import cats.data.Validated
import com.mycode.domain.ValidationError
import com.mycode.validation.UserValidation
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.circe.CirceEntityCodec.*

class UserRoutes[F[_] : {Sync, Concurrent, MonadCancelThrow}](users: Users[F]) extends Http4sDsl[F]:

  private val prefixPath = "/users"

  private def queryRoutes: HttpRoutes[F] = HttpRoutes.of[F]:
    case GET -> Root / username =>
      UserValidation.validateUsername(username) match
        case Validated.Valid(validUsername) =>
          users
            .get(validUsername)
            .flatMap:
              case Some(user) => Ok(user)
              case None       => NotFound(s"User with username $username not found")

        case Validated.Invalid(errors) =>
          BadRequest(ValidationError("Failed to validate username", errors))

    case GET -> Root / UUIDVar(userId) => Ok(s"User details for $userId")

  private def commandRoutes: HttpRoutes[F] = HttpRoutes.of[F]:
    case req @ POST -> Root =>
      req
        .as[CreateUser]
        .flatMap: createUser =>
          UserValidation.validateCreateUser(createUser) match

            case Validated.Valid(validUser) =>
              users
                .create(validUser)
                .flatMap: username =>
                  Created(s"User created with username: $username")

            case Validated.Invalid(errors) =>
              BadRequest(s"Invalid user data: ${errors.toList.mkString(", ")}")

    case req @ PUT -> Root / UUIDVar(userId) => Ok(s"User $userId updated")
    case DELETE -> Root / UUIDVar(userId)    => Ok(s"User $userId deleted")

  private def httpRoutes: HttpRoutes[F] = queryRoutes <+> commandRoutes

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)

end UserRoutes
