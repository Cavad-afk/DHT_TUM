package sssemil.com.p2p.dht.api.model

import java.util.*

data class FoundValue(val value: ByteArray) : TokenModel() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FoundValue

        if (!Arrays.equals(value, other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(value)
    }
}
