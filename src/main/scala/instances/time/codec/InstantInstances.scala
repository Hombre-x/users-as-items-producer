package com.mycode
package instances.time.codec

import io.circe.{Codec, Decoder, Encoder}

import java.time.Instant
import scala.util.Try

given Codec[Instant] = Codec.from(
  Decoder.decodeString.emapTry(str => Try(Instant.parse(str))),
  Encoder.encodeString.contramap(_.toString)
)
