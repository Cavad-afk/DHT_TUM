package sssemil.com.p2p.dht.api.model

data class Ping(var token: Double = Math.random(), val peerId: ByteArray)