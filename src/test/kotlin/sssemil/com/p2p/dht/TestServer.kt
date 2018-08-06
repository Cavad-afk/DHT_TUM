package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import sssemil.com.p2p.dht.api.DhtObj
import sssemil.com.p2p.dht.api.model.Pong
import sssemil.com.p2p.dht.util.generateKey
import sssemil.com.p2p.dht.util.generateKeyPair
import java.net.InetAddress

class TestServer {

    @Test
    fun testPing() {
        runBlocking {
            val server = Server()
            server.start()

            println("ServerID: ${server.peerId}")

            val client = Client(InetAddress.getLoopbackAddress(), server.port, server.peerId)
            client.connect()

            val mockKeyPair = generateKeyPair()

            println("MockID: $mockKeyPair")

            val response = client.ping(mockKeyPair.publicKey, 0).await()

            assert(response is DhtObj)

            assert((response as DhtObj).obj is Pong)

            assertArrayEquals(server.peerId.publicKey, (response.obj as Pong).peerId)
        }
    }

    @Test
    fun testPair() {
        runBlocking {
            val server0 = Server()
            val server1 = Server()

            server0.start()
            server1.start()

            server0.pair(InetAddress.getLoopbackAddress(), server1.port, null).await()

            println("Server0 peers:")
            list(server0)
            println("Server1 peers:")
            list(server1)

            delay(1000)

            assert(server0.peersStorage.contains(server1.peerId.publicKey))
            assert(server1.peersStorage.contains(server0.peerId.publicKey))
        }
    }

    @Test
    fun testSave() {
        runBlocking {
            val servers = Array(3) {
                val server = Server()
                server.start()
                server
            }

            for (i in (1 until servers.size - 1)) {
                servers[i].pair(InetAddress.getLoopbackAddress(), servers[servers.size / 2].port, servers[servers.size / 2].peerId.publicKey)
            }

            val clientM = Client(InetAddress.getLoopbackAddress(), servers[servers.size / 2].port, servers[servers.size / 2].peerId)
            clientM.connect()

            val testValue0 = "hi".toByteArray()
            val testValue1 = "ma nam a jaff".toByteArray()

            val testKey0 = generateKey(testValue0)
            val testKey1 = generateKey(testValue1)

            putArray(testValue0, clientM)

            clientM.stop()

            servers[0].pair(InetAddress.getLoopbackAddress(), servers[servers.size / 2].port, servers[servers.size / 2].peerId.publicKey)

            val client0 = Client(InetAddress.getLoopbackAddress(), servers[0].port, servers[0].peerId)
            client0.connect()

            putArray(testValue1, client0)

            delay(Janitor.SECOND * 10)

            val value0 = servers[0].findValue(testKey0).await()
            val value1 = servers[0].findValue(testKey1).await()

            println("Response0: ${value0?.let { String(it) } ?: "null"}")
            println("Response1: ${value1?.let { String(it) } ?: "null"}")

            assertArrayEquals(testValue1, value1)

            assertArrayEquals(testValue0, value0)
        }
    }
}