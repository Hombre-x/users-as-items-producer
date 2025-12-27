package warehouse.algebras

import cats.effect.MonadCancelThrow
import cats.syntax.all.*
import warehouse.domain.outbox.{CreateOutboxEntry, OutboxEntry}
import warehouse.domain.outbox.OutboxCodecs.*
import warehouse.domain.skunk.Pool
import warehouse.domain.skunk.UserCodecs.*
import skunk.*
import skunk.codec.all.*
import skunk.syntax.all.*

import java.util.UUID

trait Outbox[F[_]]:

  def getById(id: UUID): F[Option[OutboxEntry]]

  def persist(entry: CreateOutboxEntry): F[UUID]

  def fetchUnprocessed(limit: Int): F[List[OutboxEntry]]

  def markAsProcessed(id: UUID): F[OutboxEntry]

end Outbox

object Outbox:

  def postgres[F[_]: MonadCancelThrow](postgres: Pool[F]): Outbox[F] =
    new Outbox[F]:
      import OutboxSql.*

      override def getById(id: UUID): F[Option[OutboxEntry]] =
        postgres.use(se => se.option(selectById)(id))

      override def persist(entry: CreateOutboxEntry): F[UUID] =
        postgres.use(se => se.unique(insertOutbox)(entry))

      override def fetchUnprocessed(limit: Int): F[List[OutboxEntry]] =
        postgres.use: se =>
          se.execute(selectUnprocessed)(limit)

      override def markAsProcessed(id: UUID): F[OutboxEntry] =
        postgres.use: se =>
          se.unique(markProcessed)(id)
end Outbox

private object OutboxSql:

  // Encoders & Decoders
  (uuid *: eventId *: eventType *: username *: timestamptz *: processed).to[OutboxEntry]

  private val outboxDecoder: Decoder[OutboxEntry]          =
    (uuid ~ eventId ~ eventType ~ username ~ timestamptz ~ processed).map:
      case id ~ evId ~ evType ~ username ~ createdAt ~ proc =>
        OutboxEntry(id, evId, evType, username, createdAt, proc)
  // Scripts
  private val insertOutboxSql: Fragment[CreateOutboxEntry] =
    sql"""
      INSERT INTO outbox (event_id, event_type, username)
      VALUES ( $eventId, $eventType, $username )
      RETURNING id;
    """.to[CreateOutboxEntry]

  private val selectUnprocessedSql: Fragment[Int] =
    sql"""
      SELECT id, event_id, event_type, username, created_at, processed
      FROM outbox
      WHERE processed = FALSE
      ORDER BY created_at
      LIMIT $int4;
    """

  private val selectByIdSql: Fragment[UUID] =
    sql"""
      SELECT id, event_id, event_type, username, created_at, processed
      FROM outbox
      WHERE id = $uuid;
    """

  private val markProcessedSql: Fragment[UUID] =
    sql"""
      UPDATE outbox
      SET processed = TRUE
      WHERE id = $uuid
      RETURNING id, event_id, event_type, username, created_at, processed;
    """

  // Queries & Commands
  val selectUnprocessed: Query[Int, OutboxEntry]   = selectUnprocessedSql.query(outboxDecoder)
  val selectById: Query[UUID, OutboxEntry]         = selectByIdSql.query(outboxDecoder)
  val insertOutbox: Query[CreateOutboxEntry, UUID] = insertOutboxSql.query(uuid)
  val markProcessed: Query[UUID, OutboxEntry]      = markProcessedSql.query(outboxDecoder)

end OutboxSql
