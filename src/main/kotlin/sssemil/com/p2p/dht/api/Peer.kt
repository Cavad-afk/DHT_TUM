package sssemil.com.p2p.dht.api

import sssemil.com.p2p.dht.util.toHexString
import java.net.InetAddress
import java.util.*

open class Peer(val id: ByteArray, val ip: InetAddress, val port: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Peer

        if (!Arrays.equals(id, other.id)) return false
        if (ip != other.ip) return false
        if (port != other.port) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(id)
        result = 31 * result + ip.hashCode()
        result = 31 * result + port
        return result
    }

    override fun toString(): String {
        return "Peer(id=${id.toHexString()}, ip=$ip, port=$port)"
    }
}