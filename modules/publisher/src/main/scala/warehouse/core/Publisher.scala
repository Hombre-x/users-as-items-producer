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
import cats.instances.uuid
import warehouse.domain.user.Username
import warehouse.domain.outbox.OutboxEntry

class Publisher[F[_]: {Concurrent, Parallel, Clock, Logger as log, UUIDGen}](
    producer: Producer[F, UserCommand],
    poller: Poller[F, String],
    outbox: Outbox[F]
):

  def dummy: Stream[F, Unit] =
    Stream(
      id"warehouse_outbox_creations", 
      id"warehouse_outbox_updates", 
      id"warehouse_outbox_deletions")
      .covary[F]
      .map(poller.receive)
      .parJoin(3)
      .evalMap(nt => log.info(s"Received notification: $nt"))

  def stream: Stream[F, UserCommand] =
    processNotifications merge processOldNotifications.drain

  private def processOldNotifications: Stream[F, Unit] =
    Stream.evals(outbox.fetchUnprocessed(1000))
      .evalMap: entry =>
        for
          command <- produceEvent(entry)
            _ <- outbox.markAsProcessed(entry.id)
        yield command
      .fold(0)((count, _) => count + 1)
      .evalMap(total => log.info(s"Processed $total outbox entries."))

  private def processNotifications: Stream[F, UserCommand] =
    Stream(
      id"warehouse_outbox_creations",
      id"warehouse_outbox_updates",
      id"warehouse_outbox_deletions")
      .covary[F]
      .map(poller.receive)
      .map(stream => receiveAndProduce(stream))
      .parJoin(3)

  private def receiveAndProduce(notStream: Stream[F, String]): Stream[F, UserCommand] =
    notStream
      .evalMap(outboxIdStr => Concurrent[F].catchNonFatal(UUID.fromString(outboxIdStr)))
      .evalMap(outboxId => outbox.markAsProcessed(outboxId))
      .evalMap(produceEvent)

  private def produceEvent(entry: OutboxEntry): F[UserCommand] =
    for
      command: UserCommand <- (
        UUIDGen[F].randomUUID, 
        Clock[F].realTime.map(now => Instant.ofEpochMilli(now.toMillis)), 
        entry.username.pure[F]
        ).parMapN: (uuid, timestamp, username) =>
          entry.eventType match
            case EventType.UserCreated => UserCommand.CreateUserCommand(uuid, timestamp, username)
            case EventType.UserUpdated => UserCommand.UpdateUserCommand(uuid, timestamp, username)
            case EventType.UserDeleted => UserCommand.DeleteUserCommand(uuid, timestamp, username)
      _ <- producer.send(command)
    yield command
end Publisher

object Publisher:

  def apply[F[_]: {Concurrent, Parallel, Logger, Clock, UUIDGen}](
      producer: Producer[F, UserCommand],
      poller: Poller[F, String],
      outbox: Outbox[F]
  ): Publisher[F] =
    new Publisher[F](producer, poller, outbox)
