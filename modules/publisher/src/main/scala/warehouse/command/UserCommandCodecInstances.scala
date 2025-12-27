package warehouse.command

import io.circe.Codec
import io.circe.derivation.{Configuration, ConfiguredCodec}
import io.github.iltotore.iron.circe.given
import warehouse.domain.commands.UserCommand

given Configuration      = Configuration.default.withDiscriminator("$type")
given Codec[UserCommand] = ConfiguredCodec.derived
