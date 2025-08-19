package com.mycode
package amenities

import cats.syntax.all.*
import cats.effect.{Async, Resource, Sync}
import com.comcast.ip4s.{Port, host}
import org.http4s.{BuildInfo, HttpRoutes}
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.server.middleware.Metrics
import org.http4s.server.{HttpMiddleware, Server}
import org.typelevel.log4cats.Logger

trait MkHttpServer[F[_]]:

  def ember(port: Port, routes: HttpRoutes[F]): Resource[F, Server]
  def emberWithMetrics(port: Port, routes: HttpRoutes[F]): Resource[F, Server]

end MkHttpServer

object MkHttpServer:

  private def printVersion[F[_]](port: Port)(using log: Logger[F]): F[Unit] =
    log.info(s"Starting server with version: ${BuildInfo.version} on port: $port") 
  
  private def metrics[F[_] : Sync]: Resource[F, HttpMiddleware[F]] =
    for
      metricsService <- PrometheusExportService.build[F]
      metrics <- Prometheus.metricsOps[F](metricsService.collectorRegistry)
    yield apiRoutes => Metrics[F](metrics)(metricsService.routes <+> apiRoutes)

  def make[F[_] : {Async, Logger}]: MkHttpServer[F] = new MkHttpServer[F]:
    
    override def ember(port: Port, routes: HttpRoutes[F]): Resource[F, Server] =
      EmberServerBuilder
        .default[F]
        .withHost(host"0.0.0.0")
        .withPort(port)
        .withHttpApp(routes.orNotFound)
        .build
        .evalTap(_ => printVersion(port))

    override def emberWithMetrics(port: Port, routes: HttpRoutes[F]): Resource[F, Server] = 
      metrics[F].flatMap: middleware =>
        EmberServerBuilder
          .default[F]
          .withHost(host"0.0.0.0")
          .withPort(port)
          .withHttpApp(middleware(routes).orNotFound)
          .build
          .evalTap(_ => printVersion(port))

  

