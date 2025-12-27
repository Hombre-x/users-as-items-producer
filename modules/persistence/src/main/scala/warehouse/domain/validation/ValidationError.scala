package warehouse.domain.validation

import cats.data.NonEmptyList
import io.circe.Codec

import scala.util.control.NoStackTrace

case class ValidationError(message: String, errors: NonEmptyList[String]) extends NoStackTrace derives Codec
