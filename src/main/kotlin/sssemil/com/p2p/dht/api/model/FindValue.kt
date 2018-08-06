package sssemil.com.p2p.dht.api.model

import sssemil.com.p2p.dht.util.toHexString
import java.util.*

data class FindValue(val publicKey: ByteArray, val key: ByteArray) : TokenModel() {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FindValue

        if (!Arrays.equals(publicKey, other.publicKey)) return false
        if (!Arrays.equals(key, other.key)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(publicKey)
        result = 31 * result + Arrays.hashCode(key)
        return result
    }

    override fun toString(): String {
        return "FindValue(publicKey=${publicKey.toHexString()}, key=${key.toHexString()})"
    }
}