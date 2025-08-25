package com.mycode
package http.routes

import cats.effect.Temporal
import cats.syntax.option.*

import org.http4s.*
import org.http4s.server.Router
import org.http4s.dsl.*
import org.http4s.circe.CirceEntityCodec.*

import fs2.Stream

import scala.concurrent.duration.*

final class HealthRoutes[F[_]: Temporal] extends Http4sDsl[F]:

  private val prefixPath = "/"

  private def httpRoutes: HttpRoutes[F] = HttpRoutes.of[F]:
    case GET -> Root / "health"   => Ok("Safe and sound!")
    case GET -> Root / "infinite" =>
      Ok(
        Stream
          .awakeEvery[F](1.second)
          .map(n => ServerSentEvent(data = s"Created $n".some, eventType = "time".some))
      )

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)

end HealthRoutes
