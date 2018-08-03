package sssemil.com.p2p.dht.util

import sssemil.com.p2p.dht.Server
import sssemil.com.p2p.dht.api.*
import java.util.*

class PeerStorage {
    val id: ByteArray = generateId()

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

    fun add(peer: Peer, peerId: ByteArray) {
        val distance = IdKeyUtils.distance(peer.id, peerId)

        Logger.i("Add peer $peer at $distance")

        peers[distance].add(PeerHolder(peer.id, peer.ip, peer.port,
                System.currentTimeMillis(), System.currentTimeMillis()))
    }

    fun contains(peerId: ByteArray): Boolean {
        val distance = IdKeyUtils.distance(id, peerId)

        return peers[distance].any { it.id.contentEquals(peerId) }
    }
}