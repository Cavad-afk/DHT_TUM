package sssemil.com.p2p.dht.api

import sssemil.com.p2p.dht.Janitor

internal const val DHT_PUT = 650.toShort()
internal const val DHT_GET = 651.toShort()
internal const val DHT_SUCCESS = 652.toShort()
internal const val DHT_FAILURE = 653.toShort()
internal const val DHT_OBJ = 654.toShort()

internal const val OBJ_PING = 1
internal const val OBJ_PONG = 2
internal const val OBJ_PUT = 3
internal const val OBJ_FIND_VALUE = 4
internal const val OBJ_FOUND_VALUE = 5
internal const val OBJ_FOUND_PEERS = 6

internal const val RSA_KEY_LENGTH = 4096 // bits
internal const val KEY_LENGTH = 128 / 8 // bytes (md5 size)
internal const val TOLERANCE = 16 * 8 // distance tolerance, decrease it in larger networks
internal const val BUCKETS = 20
internal const val GRAB_SIZE = 3

internal const val DEFAULT_TTL = Janitor.DAY.toShort()
internal const val DEFAULT_REPLICATION = 5.toByte()
internal const val DEFAULT_DELAY = 10000 // 5s
internal const val PING_DELAY = 5000 // 5s