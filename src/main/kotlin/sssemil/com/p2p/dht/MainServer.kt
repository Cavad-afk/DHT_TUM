package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.runBlocking
import sssemil.com.p2p.dht.api.KEY_LENGTH

fun main(args: Array<String>) {
    runBlocking {
        Server(2000, ByteArray(KEY_LENGTH)).start().await()
    }
}
