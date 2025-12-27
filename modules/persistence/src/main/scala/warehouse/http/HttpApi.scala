package warehouse.http

import cats.data.OptionT
import cats.effect.Temporal
import cats.effect.kernel.MonadCancelThrow
import cats.syntax.all.*
import org.http4s.HttpRoutes
import org.http4s.server.middleware.{CORS, ErrorAction, ErrorHandling}
import org.typelevel.log4cats.Logger
import warehouse.core.Warehouse
import warehouse.http.middleware.ValidationMiddleware
import warehouse.http.routes.{HealthRoutes, UserRoutes}

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

  def make[F[_]: {Temporal, MonadCancelThrow, Logger}](warehouse: Warehouse[F]): HttpApi[F] =
    new HttpApi[F](warehouse)

end HttpApi
