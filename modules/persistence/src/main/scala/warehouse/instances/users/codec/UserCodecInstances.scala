package warehouse.instances.users.codec

import cats.effect.Concurrent
import org.http4s.circe.*
import org.http4s.{EntityDecoder, EntityEncoder}
import warehouse.domain.user.CreateUser

// Http4s EntityEncoder and EntityDecoder
given [F[_]]: EntityEncoder[F, CreateUser]              = jsonEncoderOf[F, CreateUser]
given [F[_] : Concurrent]: EntityDecoder[F, CreateUser] = accumulatingJsonOf[F, CreateUser]
