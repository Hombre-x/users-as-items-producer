package com.mycode
package http


import cats.MonadThrow
import org.http4s.server.middleware.{CORS, ErrorHandling, Metrics}
import http.routes.HealthRoutes

import org.http4s.HttpRoutes

class HttpApi[F[_] : MonadThrow]:
  
  private val healthRoutes = HealthRoutes[F].routes
    
  private val allRoutes = healthRoutes

  private def corsRoutes(routes: HttpRoutes[F]): HttpRoutes[F] =
    CORS.policy.withAllowOriginAll(routes)

  private def errorHandlingRoutes(routes: HttpRoutes[F]): HttpRoutes[F] =
    ErrorHandling.Recover.messageFailure(routes)
    
  def routes: HttpRoutes[F] = (corsRoutes andThen errorHandlingRoutes)(allRoutes)

end HttpApi

object HttpApi:
  
  def make[F[_] : MonadThrow]: HttpApi[F] = new HttpApi[F]

end HttpApi
