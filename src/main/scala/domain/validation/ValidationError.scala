package com.mycode
package domain.validation

import scala.util.control.NoStackTrace
import cats.data.NonEmptyList
import io.circe.Codec

case class ValidationError(message: String, errors: NonEmptyList[String]) extends NoStackTrace derives Codec
