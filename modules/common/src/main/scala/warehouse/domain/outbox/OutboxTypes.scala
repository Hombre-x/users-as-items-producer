package warehouse.domain.outbox

import warehouse.domain.EventType

import java.time.OffsetDateTime
import java.util.UUID
import warehouse.domain.user.Username

case class OutboxEntry(
    id: UUID,
    eventId: UUID,
    eventType: EventType,
    username: Username,
    createdAt: OffsetDateTime,
    processed: Boolean
)

case class CreateOutboxEntry(
    eventId: UUID,
    eventType: EventType,
    username: Username
)

case class OutboxEntryNotFound(id: UUID) extends Exception(s"Outbox entry with id $id not found.")
