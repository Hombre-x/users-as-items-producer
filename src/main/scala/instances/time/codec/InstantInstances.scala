package com.mycode
package instances.time.codec

import scodec.*
import scodec.codecs.*
import java.time.Instant

given Codec[Instant] = long(64).xmap[Instant](Instant.ofEpochMilli, _.toEpochMilli)
