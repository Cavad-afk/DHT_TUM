package sssemil.com.p2p.dht.api.model

import sssemil.com.p2p.dht.api.*

open class TokenModel(var token: Double = Math.random()) {
    companion object {
        fun secure(code: Int) = when (code) {
            OBJ_PING,
            OBJ_PONG -> false
            else -> false // TODO for now
        }

        fun objectCode(objClass: Class<*>) = when (objClass) {
            Ping::class.java -> OBJ_PING
            Pong::class.java -> OBJ_PONG
            Put::class.java -> OBJ_PUT
            FindValue::class.java -> OBJ_FIND_VALUE
            FoundValue::class.java -> OBJ_FOUND_VALUE
            FoundPeers::class.java -> OBJ_FOUND_PEERS
            else -> 0
        }
    }
}
