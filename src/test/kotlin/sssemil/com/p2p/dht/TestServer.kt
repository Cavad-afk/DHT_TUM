package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import sssemil.com.p2p.dht.api.DhtObj
import sssemil.com.p2p.dht.api.DhtSuccess
import sssemil.com.p2p.dht.api.model.Pong
import sssemil.com.p2p.dht.util.generateKey
import sssemil.com.p2p.dht.util.generateKeyPair
import java.net.InetAddress
import java.util.*

class TestServer {

    @Test
    fun testPing() {
        runBlocking {
            val server = Server()
            server.start().await()

            println("ServerID: ${server.peerId}")

            val client = Client(InetAddress.getLoopbackAddress(), server.port, server.peerId)
            client.connect().await()

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

            server0.start().await()
            server1.start().await()

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
            val server0 = Server()
            val server1 = Server()

            server0.start().await()
            server1.start().await()

            server0.pair(InetAddress.getLoopbackAddress(), server1.port, server1.peerId.publicKey).await()

            val client = Client(InetAddress.getLoopbackAddress(), server0.port, server0.peerId)
            client.connect().await()

            val testValue = ByteArray(500)
            Random().nextBytes(testValue)
            val testKey = generateKey(testValue)

            putArray(testValue, client)

            delay(2000)

            val response = getValue(testKey, client).await()

            assert(response is DhtSuccess)
            response as DhtSuccess

            assertArrayEquals(testKey, response.key)

            assertArrayEquals(testValue, response.value)
        }
    }
}