package sssemil.com.p2p.dht.api.model

import sssemil.com.p2p.dht.util.toHexString
import java.util.*

data class FindValue(val key: ByteArray) : TokenModel() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FindValue

        if (!Arrays.equals(key, other.key)) return false

        return true
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(key)
    }

    override fun toString(): String {
        return "FindValue(key=${key.toHexString()})"
    }
}