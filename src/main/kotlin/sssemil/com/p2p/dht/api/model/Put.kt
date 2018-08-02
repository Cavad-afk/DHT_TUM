package sssemil.com.p2p.dht.api.model

import sssemil.com.p2p.dht.util.generateKey

data class Put(val ttl: Short, var replicationsLeft: Byte, val value: ByteArray) : TokenModel() {
    val key: ByteArray
        get() = generateKey(value)
}