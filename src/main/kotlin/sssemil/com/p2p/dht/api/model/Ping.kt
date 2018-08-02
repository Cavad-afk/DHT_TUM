package sssemil.com.p2p.dht.api.model

data class Ping(val peerId: ByteArray, val port: Int) : TokenModel()