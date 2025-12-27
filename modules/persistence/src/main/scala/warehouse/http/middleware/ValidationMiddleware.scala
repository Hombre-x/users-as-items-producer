package warehouse.http.middleware

import cats.data.*
import cats.{Applicative, MonadThrow}
import io.circe.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.*
import org.http4s.dsl.Http4sDsl
import org.http4s.server.middleware.ErrorHandling
import warehouse.domain.validation.ValidationError

object ValidationMiddleware:

  private def badApiRequest[F[_] : Applicative](errors: NonEmptyList[String]): OptionT[F, Response[F]] =

    val dsl = Http4sDsl[F]
    import dsl.*
    OptionT.liftF(BadRequest(ValidationError("Validation error at API", errors)))

  end badApiRequest

  def errorHandlingRoutes[F[_] : MonadThrow](routes: HttpRoutes[F]): HttpRoutes[F] =
    ErrorHandling.Custom.recoverWith(routes):
      case InvalidMessageBodyFailure(defaultMessage, cause)   =>
        cause match
          case Some(DecodingFailure(message, _)) => badApiRequest(NonEmptyList.one(message))
          case Some(DecodingFailures(failures))  => badApiRequest(failures.map(_.message))
          case _                                 => badApiRequest(NonEmptyList.one(defaultMessage))
      case MalformedMessageBodyFailure(defaultMessage, cause) =>
        cause match
          case Some(ParsingFailure(message, _)) => badApiRequest(NonEmptyList.one(message))
          case _                                => badApiRequest(NonEmptyList.one(defaultMessage))

end ValidationMiddleware
