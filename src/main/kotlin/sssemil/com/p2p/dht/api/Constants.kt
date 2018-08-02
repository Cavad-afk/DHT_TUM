package sssemil.com.p2p.dht.api

internal const val DHT_PUT = 650.toShort()
internal const val DHT_GET = 651.toShort()
internal const val DHT_SUCCESS = 652.toShort()
internal const val DHT_FAILURE = 653.toShort()
internal const val DHT_OBJ = 654.toShort()

internal const val DHT_PING = 1
internal const val DHT_PONG = 2
internal const val DHT_FIND_NODE = 3
internal const val DHT_FIND_VALUE = 4
internal const val DHT_ERROR = 5
internal const val DHT_NODE = 6
internal const val DHT_VALUE = 7

internal const val KEY_LENGTH = 16 // bytes
internal const val TOLERANCE = 15 // bytes
internal const val BUCKETS = 20

internal const val DEFAULT_DELAY = 10000 // 5s
internal const val PING_DELAY = 5000 // 5s