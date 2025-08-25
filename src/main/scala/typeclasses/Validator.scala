package com.mycode.typeclasses

import cats.data.ValidatedNec

trait Validator[A]:

  def validate(input: A): ValidatedNec[String, A]

object Validator:

  def apply[A](using Validator[A]): Validator[A] = summon[Validator[A]]
