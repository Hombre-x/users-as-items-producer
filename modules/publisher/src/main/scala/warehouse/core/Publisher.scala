package warehouse.core

import cats.Parallel
import cats.syntax.all.*
import cats.effect.{Clock, Concurrent}
import cats.effect.std.UUIDGen
import fs2.Stream
import org.typelevel.log4cats.Logger
import skunk.syntax.all.*
import warehouse.algebras.{Outbox, Poller, Producer}
import warehouse.domain.commands.UserCommand
import warehouse.domain.EventType

import java.util.UUID
import java.time.Instant
import warehouse.domain.outbox.{OutboxEntry, OutboxEntryNotFound}

class Publisher[F[_]: {Concurrent, Parallel, Clock, Logger as log}](
    producer: Producer[F, UserCommand],
    poller: Poller[F, String],
    outbox: Outbox[F]
):

  def dummy: Stream[F, Unit] =
    Stream(id"warehouse_outbox_creations", id"warehouse_outbox_updates", id"warehouse_outbox_deletions")
      .covary[F]
      .map(poller.receive)
      .parJoin(3)
      .evalMap(nt => log.info(s"Received notification: $nt"))

  def stream: Stream[F, UserCommand] =
    processNotifications concurrently processOldNotifications

  private def processOldNotifications: Stream[F, Unit] =
    Stream
      .evals(outbox.fetchUnprocessed(1000))
      .evalMap: entry =>
        for
          command <- produceEvent(entry)
          _       <- outbox.markAsProcessed(entry.id)
        yield command
      .fold(0)((count, _) => count + 1)
      .evalMap(total => log.info(s"Processed $total outbox entries."))

  private def processNotifications: Stream[F, UserCommand] =
    Stream(id"warehouse_outbox_creations", id"warehouse_outbox_updates", id"warehouse_outbox_deletions")
      .covary[F]
      .map(poller.receive)
      .map(stream => receiveAndProduce(stream))
      .parJoin(3)

  private def receiveAndProduce(notStream: Stream[F, String]): Stream[F, UserCommand] =
    notStream
      .evalMap(outboxIdStr => Concurrent[F].catchNonFatal(UUID.fromString(outboxIdStr)))
      .evalMap: outboxId =>
        outbox
          .getById(outboxId)
          .flatMap:
            case Some(entry) => entry.pure[F]
            case None        =>
              log.warn(s"Outbox entry with id $outboxId not found.") >>
                OutboxEntryNotFound(outboxId).raiseError[F, OutboxEntry]
              
      .evalMap: entry =>
        for
          command <- produceEvent(entry)
          _       <- outbox.markAsProcessed(entry.id)
        yield command

  private def produceEvent(entry: OutboxEntry): F[UserCommand] =
    (
      Clock[F].realTime.map(now => Instant.ofEpochMilli(now.toMillis)),
      entry.username.pure[F]
    ).parMapN: (timestamp, username) =>
      entry.eventType match
        case EventType.UserCreated => UserCommand.CreateUserCommand(entry.eventId, timestamp, username)
        case EventType.UserUpdated => UserCommand.UpdateUserCommand(entry.eventId, timestamp, username)
        case EventType.UserDeleted => UserCommand.DeleteUserCommand(entry.eventId, timestamp, username)
    .flatMap(command => producer.send(command).as(command))

end Publisher

object Publisher:

  def apply[F[_]: {Concurrent, Parallel, Logger, Clock}](
      producer: Producer[F, UserCommand],
      poller: Poller[F, String],
      outbox: Outbox[F]
  ): Publisher[F] =
    new Publisher[F](producer, poller, outbox)
