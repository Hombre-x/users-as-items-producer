package com.mycode
package amenities

import cats.effect.Resource

import domain.config.AppConfig

case class AppResources[F[_]]()

object AppResources:
  def make[F[_]](config: AppConfig): Resource[F, AppResources[F]] = Resource.pure(AppResources())
