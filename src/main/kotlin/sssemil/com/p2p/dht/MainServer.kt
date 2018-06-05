package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.runBlocking

fun main(args: Array<String>) {
    runBlocking {
        val servers = (0..20).map { Server(2000 + it) }
        servers.forEach { it.start() }
    }
}
