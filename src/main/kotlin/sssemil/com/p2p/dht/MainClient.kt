package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.runBlocking
import sssemil.com.p2p.dht.util.Logger

fun main(args: Array<String>) {
    runBlocking {
        val client = Client("127.0.0.1", 2000)
        //client.putValue("12345678901234567890123456789012".toByteArray(), "Test Value".toByteArray())

        val response = client.getValue("12345678901234567890123456789012".toByteArray()).await()
        Logger.i(response.toString())
    }
}
