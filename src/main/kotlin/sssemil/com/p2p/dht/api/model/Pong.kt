package sssemil.com.p2p.dht.api.model

import sssemil.com.p2p.dht.util.toHexString
import java.util.*

data class Pong(val tokenReply: Double, val peerId: ByteArray, val port: Int) : TokenModel(tokenReply) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Pong

        if (!Arrays.equals(peerId, other.peerId)) return false
        if (port != other.port) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(peerId)
        result = 31 * result + port
        return result
    }

    override fun toString(): String {
        return "Pong(token=$token, peerId=${peerId.toHexString()}, port=$port)"
    }
}