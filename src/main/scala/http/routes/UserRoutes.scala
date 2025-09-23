package com.mycode
package http.routes

import cats.effect.{Concurrent, MonadCancelThrow}
import cats.syntax.all.*
import domain.user.*
import instances.users.codec.given

import core.Warehouse
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.server.Router
import org.http4s.circe.CirceEntityCodec.*

class UserRoutes[F[_]: {Concurrent, MonadCancelThrow}](warehouse: Warehouse[F]) extends Http4sDsl[F]:

  private val prefixPath = "/users"

  private def queryRoutes: HttpRoutes[F] = HttpRoutes.of[F]:
    case GET -> Root / username => Ok(s"To be implemented, $username")

  private def commandRoutes: HttpRoutes[F] = HttpRoutes.of[F]:
    case req @ POST -> Root =>
      req
        .as[CreateUser]
        .flatMap: createUser =>
          warehouse
            .addUser(createUser)
            .flatMap: username =>
              Created(s"User with username $username created")
            .recoverWith:
              case UsernameAlreadyExists(username) =>
                Conflict(s"User with username $username already exists")

    case req @ PUT -> Root =>
      req
        .as[UpdateUser]
        .flatMap: updateUser =>
          warehouse
            .updateUser(updateUser)
            .flatMap: username =>
              Ok(s"User with username $username updated")
            .recoverWith:
              case UserNotFound(username) =>
                NotFound(s"User with username $username not found")

    case DELETE -> Root / username =>
      warehouse
        .deleteUser(Username.applyUnsafe(username))
        .flatMap:
          case true  => Ok(s"User with username $username deleted")
          case false => NotFound(s"User with username $username not found")

  private def httpRoutes: HttpRoutes[F] = queryRoutes <+> commandRoutes

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)

end UserRoutes
