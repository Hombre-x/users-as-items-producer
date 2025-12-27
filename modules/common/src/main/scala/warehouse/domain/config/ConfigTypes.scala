package warehouse.domain.config

import ciris.Secret
import com.comcast.ip4s.Port



case class ProducerConfig(bootstrapServer: String)
case class ConsumerConfig(bootstrapServer: String, groupId: String)
case class HttpConfig(host: String, port: Port)
case class PostgreSQLConfig(host: String, port: Int, user: String, password: Secret[String], database: String)
