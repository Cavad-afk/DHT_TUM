package sssemil.com.p2p.dht.api.model

data class Pong(val peerId: ByteArray, val port: Int) : TokenModel()