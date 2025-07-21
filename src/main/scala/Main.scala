package com.mycode

import cats.effect.{IO, IOApp}
import cats.effect.std.UUIDGen
import org.typelevel.log4cats.Logger
import org.typelevel.log4cats.slf4j.Slf4jLogger
import fs2.kafka.KafkaProducer
import amenities.MkKafkaProducerSettings
import config.Config

object Main extends IOApp.Simple:

  given logger: Logger[IO] = Slf4jLogger.getLoggerFromName("Main")
  
  override def run: IO[Unit] =
    
    Config.load[IO].flatMap: config =>
        logger.info(s"Loading config for application: $config") >>
          MkKafkaProducerSettings .make[IO](config.producerConfig).create.use: producerSettings =>
            
              KafkaProducer
                .stream(producerSettings)
                .evalMap(producer => UUIDGen.randomUUID[IO].map(uuid => (uuid, producer)))
                .evalMap:
                  case (uuid, producer) =>
                    producer.produceOne("warehouse-topic", uuid, "Hi there Mr. Kafka! 🍎")
                .compile
                .drain
  end run
  

end Main
