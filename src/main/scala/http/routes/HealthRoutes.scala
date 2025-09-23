package com.mycode
package http.routes

import cats.effect.Temporal
import cats.syntax.option.*
import fs2.Stream
import org.http4s.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.*
import org.http4s.server.Router

import scala.concurrent.duration.*

final class HealthRoutes[F[_]: Temporal] extends Http4sDsl[F]:

  private val prefixPath = "/health"

  private def httpRoutes: HttpRoutes[F] = HttpRoutes.of[F]:
    case GET -> Root              => Ok("Safe and sound!")
    case GET -> Root / "infinite" =>
      Ok(
        Stream
          .iterate(0)(_ + 1)
          .map(n => ServerSentEvent(data = s"Created $n".some, eventType = "time".some))
          .metered(1.second)
      )

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)

end HealthRoutes
