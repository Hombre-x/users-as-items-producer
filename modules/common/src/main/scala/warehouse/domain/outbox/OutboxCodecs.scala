package warehouse.domain.outbox

import cats.syntax.either.*
import skunk.Codec
import skunk.codec.all.*
import skunk.data.Type
import warehouse.domain.EventType

import java.util.UUID

object OutboxCodecs:

  val eventId: Codec[UUID]         = uuid
  val processed: Codec[Boolean]    = bool
  val eventType: Codec[EventType]  = Codec.simple(
    _.toString, 
    eStr => Either.catchNonFatal(EventType.valueOf(eStr)).leftMap(_.getMessage),
    Type("text"))

end OutboxCodecs

