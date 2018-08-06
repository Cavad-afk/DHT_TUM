package sssemil.com.p2p.dht.api.model

import sssemil.com.p2p.dht.api.Peer
import java.util.*

data class FoundPeers(val tokenReply: Double, val peers: Array<Peer>) : TokenModel(tokenReply) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FoundPeers

        if (!Arrays.equals(peers, other.peers)) return false

        return true
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(peers)
    }

    override fun toString(): String {
        return "FoundPeers(token=$token, peers=${Arrays.toString(peers)})"
    }
}
