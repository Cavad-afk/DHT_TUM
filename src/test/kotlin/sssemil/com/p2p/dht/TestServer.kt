package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.runBlocking
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import sssemil.com.p2p.dht.api.DhtObj
import sssemil.com.p2p.dht.api.model.Pong
import sssemil.com.p2p.dht.util.generateId
import java.net.InetAddress

class TestServer {

    @Test
    fun testPing() {
        runBlocking {
            val server = Server()
            server.start().await()

            println("ServerID: ${server.peerId}")

            val client = Client(InetAddress.getLoopbackAddress(), server.port)
            client.connect().await()

            val mockId = generateId()

            println("MockID: $mockId")

            val response = client.ping(mockId, 0).await()

            assert(response is DhtObj)

            assert((response as DhtObj).obj is Pong)

            assertArrayEquals(server.peerId, (response.obj as Pong).peerId)
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

            assert(server0.peersStorage.contains(server1.peerId))
            assert(server1.peersStorage.contains(server0.peerId))
        }
    }
}