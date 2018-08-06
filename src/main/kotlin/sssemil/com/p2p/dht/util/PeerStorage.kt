package sssemil.com.p2p.dht.util

import sssemil.com.p2p.dht.Server
import sssemil.com.p2p.dht.api.*
import java.util.*

class PeerStorage {
    val id: KeyPair = generateKeyPair()

    val peers = Array(KEY_LENGTH * 8) { SortedList<PeerHolder>(BUCKETS) }

    fun findClosest(key: ByteArray, exceptions: HashSet<String>): LinkedList<Server.WeightedPeer> {
        val tmp = SortedList<Server.WeightedPeer>(GRAB_SIZE)
        peers.iterator().forEach { sortedList ->
            sortedList.forEach {
                if (!exceptions.contains(it.id.toHexString())) {
                    tmp.add(Server.WeightedPeer(IdKeyUtils.distance(key, it.id), it))
                }
            }
        }

        return tmp.list
    }

    fun update(peer: Peer) {
        val distance = IdKeyUtils.distance(peer.id, id.publicKey)

        val sub = peers[distance]

        val peerHolder = sub.firstOrNull { it.id.contentEquals(peer.id) }
        peerHolder?.lastSeen = System.currentTimeMillis()

        if (peerHolder == null) {
            add(peer)
        }
    }

    fun add(peer: Peer) {
        if (contains(peer.id)) {
            Logger.e("Not adding, already have: ${peer.id.toHexString()}")
            return
        }

        if (peer.id.contentEquals(id.publicKey)) {
            Logger.e("Not adding self: ${peer.id.toHexString()}")
            return
        }

        val distance = IdKeyUtils.distance(peer.id, id.publicKey)

        Logger.i("Add peer $peer at $distance")

        peers[distance].add(PeerHolder(peer.id, peer.ip, peer.port,
                System.currentTimeMillis(), System.currentTimeMillis()))
    }

    fun contains(peerId: ByteArray): Boolean {
        val distance = IdKeyUtils.distance(id.publicKey, peerId)

        return peers[distance].any { it.id.contentEquals(peerId) }
    }

    override fun toString(): String {
        return "PeerStorage(id=$id, peers=${peers.joinToString("") {
            if (it.isNotEmpty()) it.joinToString("", postfix = ";") else ""
        }})"
    }
}