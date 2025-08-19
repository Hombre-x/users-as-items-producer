package com.mycode
package domain.skunk

import cats.effect.Resource
import skunk.Session

type Pool[F[_]] = Resource[F, Session[F]]