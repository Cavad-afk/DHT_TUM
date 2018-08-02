package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.runBlocking

fun main(args: Array<String>) {
    runBlocking {
        Server(2000).start().await()
    }
}
