package sssemil.com.p2p.dht.api

interface DhtMessage {
    fun generate(): ByteArray
}