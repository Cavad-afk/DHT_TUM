package sssemil.com.p2p.dht.api

internal const val DHT_PUT = 650.toShort()
internal const val DHT_GET = 651.toShort() // this will be used by other modules
internal const val DHT_GET_INTERNAL = 652.toShort() // this will be used among DHT modules only
internal const val DHT_SUCCESS = 652.toShort()
internal const val DHT_FAILURE = 653.toShort()

internal const val KEY_LENGTH = 32