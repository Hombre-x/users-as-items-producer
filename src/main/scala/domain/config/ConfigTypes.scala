package com.mycode
package domain.config

case class AppConfig(
    producerConfig: ProducerConfig,
    consumerConfig: ConsumerConfig
)

case class ProducerConfig(bootstrapServer: String)
case class ConsumerConfig(bootstrapServer: String, groupId: String)
