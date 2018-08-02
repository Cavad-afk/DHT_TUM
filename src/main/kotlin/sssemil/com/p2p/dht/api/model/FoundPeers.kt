package sssemil.com.p2p.dht.api.model

import sssemil.com.p2p.dht.api.Peer
import java.util.*

data class FoundPeers(val peers: Array<Peer>) : TokenModel() {
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
}
