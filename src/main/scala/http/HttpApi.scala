package com.mycode
package http

import cats.syntax.all.*
import cats.effect.{Sync, Temporal}
import org.http4s.server.middleware.{CORS, ErrorAction, ErrorHandling}
import http.routes.{HealthRoutes, UserRoutes}

import cats.data.OptionT
import cats.effect.kernel.MonadCancelThrow
import com.mycode.core.Warehouse
import com.mycode.http.middleware.ValidationMiddleware
import org.http4s.HttpRoutes
import org.typelevel.log4cats.Logger

class HttpApi[F[_]: {Temporal, MonadCancelThrow, Logger as log}] private (warehouse: Warehouse[F]):

  private val healthRoutes = HealthRoutes[F].routes
  private val userRoutes   = UserRoutes[F](warehouse).routes

  private val allRoutes = healthRoutes <+> userRoutes

  private def corsRoutes(routes: HttpRoutes[F]): HttpRoutes[F] =
    CORS.policy.withAllowOriginAll(routes)

  private def errorHandlingRoutes(routes: HttpRoutes[F]): HttpRoutes[F] =
    def errorHandler(t: Throwable, msg: => String): OptionT[F, Unit] =
      OptionT.liftF(log.error(t)(msg))

    ErrorHandling.Recover.total(
      ErrorAction.log(
        routes,
        messageFailureLogAction = errorHandler,
        serviceErrorLogAction = errorHandler
      )
    )

  def routes: HttpRoutes[F] = (
    corsRoutes andThen
      ValidationMiddleware.errorHandlingRoutes andThen
      errorHandlingRoutes
  )(allRoutes)

end HttpApi

object HttpApi:

  def make[F[_]: {Temporal, Sync, MonadCancelThrow, Logger}](warehouse: Warehouse[F]): HttpApi[F] =
    new HttpApi[F](warehouse)

end HttpApi
