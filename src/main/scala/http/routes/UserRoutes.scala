package com.mycode
package http.routes

import cats.effect.{Concurrent, MonadCancelThrow, Sync}
import cats.syntax.all.*
import algebras.Users
import domain.user.*

import cats.data.Validated

import com.mycode.validation.UserValidation
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.circe.CirceEntityCodec.*

class UserRoutes[F[_]: {Sync, Concurrent, MonadCancelThrow}](users: Users[F]) extends Http4sDsl[F]:

  private val prefixPath = "/users"

  private def queryRoutes: HttpRoutes[F] = HttpRoutes.of[F]:
    case GET -> Root / username =>

      users
        .get(Username.applyUnsafe(username))
        .flatMap:
          case Some(user) => Ok(user)
          case None       => NotFound(s"User with username $username not found")

    case GET -> Root / UUIDVar(userId) =>
      users
        .getById(userId)
        .flatMap:
          case Some(user) => Ok(user)
          case None       => NotFound(s"User with ID $userId not found")

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

    case _ @PUT -> Root / UUIDVar(userId) => Ok(s"User $userId updated")
    case DELETE -> Root / UUIDVar(userId) => Ok(s"User $userId deleted")

  private def httpRoutes: HttpRoutes[F] = queryRoutes <+> commandRoutes

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)

end UserRoutes
