package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import sssemil.com.p2p.dht.api.DhtGet
import sssemil.com.p2p.dht.api.KEY_LENGTH
import sssemil.com.p2p.dht.util.Logger

fun main(args: Array<String>) {
    val dhtGet = DhtGet(ByteArray(KEY_LENGTH) { 62 })

    runBlocking {
        val client = Client("127.0.0.1", 2000)

        while (true) {
            Logger.i("PING: ${client.ping().await()}")
            delay(2000)
        }
    }
}
