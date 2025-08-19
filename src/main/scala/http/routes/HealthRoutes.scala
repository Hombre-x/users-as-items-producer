package com.mycode
package http.routes

import cats.Monad
import org.http4s.*
import org.http4s.server.Router
import org.http4s.dsl.*

final class HealthRoutes[F[_] : Monad] extends Http4sDsl[F]:
  
  private val prefixPath = "/"
  
  private def httpRoutes: HttpRoutes[F] = HttpRoutes.of[F]:
    case GET -> Root / "health" => Ok("Safe and sound!")
  

  val routes: HttpRoutes[F] = Router(prefixPath -> httpRoutes)
  
end HealthRoutes

  
  
  
