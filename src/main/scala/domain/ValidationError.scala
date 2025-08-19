package com.mycode
package domain

import cats.data.NonEmptyList
import io.circe.Codec

case class ValidationError(message: String, errors: NonEmptyList[String]) derives Codec
