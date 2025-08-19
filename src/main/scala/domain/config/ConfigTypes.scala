package com.mycode
package domain.config

import com.comcast.ip4s.Port

case class AppConfig(
    producerConfig: ProducerConfig,
    consumerConfig: ConsumerConfig,
    httpConfig: HttpConfig
)

case class ProducerConfig(bootstrapServer: String)
case class ConsumerConfig(bootstrapServer: String, groupId: String)
case class HttpConfig(host: String, port: Port)
