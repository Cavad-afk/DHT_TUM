package sssemil.com.p2p.dht.api.model

import sssemil.com.p2p.dht.util.generateKey
import java.util.*

data class Put(val ttl: Long, var replicationsLeft: Byte, val value: ByteArray) : TokenModel() {
    val key: ByteArray
        get() = generateKey(value)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Put

        if (ttl != other.ttl) return false
        if (replicationsLeft != other.replicationsLeft) return false
        if (!Arrays.equals(value, other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ttl.toInt()
        result = 31 * result + replicationsLeft
        result = 31 * result + Arrays.hashCode(value)
        return result
    }
}