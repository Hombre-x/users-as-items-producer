package com.mycode
package instances.users.codec

import scodec.Codec
import cats.effect.Concurrent

import org.http4s.{EntityDecoder, EntityEncoder}
import org.http4s.circe.*

import io.github.iltotore.iron.scodec.given

import domain.user.*

// Http4s EntityEncoder and EntityDecoder
given [F[_]]: EntityEncoder[F, CreateUser]             = jsonEncoderOf[F, CreateUser]
given [F[_]: Concurrent]: EntityDecoder[F, CreateUser] = accumulatingJsonOf[F, CreateUser]

given Codec[CreateUser] = Codec.derived
given Codec[UpdateUser] = Codec.derived
given Codec[User]       = Codec.derived
