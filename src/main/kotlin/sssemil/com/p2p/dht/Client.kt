package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import sssemil.com.p2p.dht.api.*
import sssemil.com.p2p.dht.api.model.Ping
import sssemil.com.p2p.dht.api.model.Pong
import sssemil.com.p2p.dht.util.ActiveList
import sssemil.com.p2p.dht.util.Logger
import sssemil.com.p2p.dht.util.isAlive
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ConnectException
import java.net.InetAddress
import java.net.Socket

class Client(private val serverAddress: InetAddress, val serverPort: Int) {
    private lateinit var clientSocket: Socket
    private lateinit var outToServer: DataOutputStream
    private lateinit var inFromServer: DataInputStream

    private val responses = ActiveList<DhtMessage>()

    private fun setupConnection() = async {
        try {
            clientSocket = Socket(serverAddress, serverPort)
            outToServer = DataOutputStream(clientSocket.getOutputStream())
            inFromServer = DataInputStream(clientSocket.getInputStream())

            monitor()

            return@async true
        } catch (e: ConnectException) {
            Logger.e(e.localizedMessage)
        }

        return@async false
    }

    private fun monitor() = async {
        while (clientSocket.isAlive) {
            if (inFromServer.available() > 0) {
                seekResponse()
            }

            delay(10)
        }

        Logger.w("Socket closed!")
    }

    fun ping(peerId: ByteArray) = async {
        if (!clientSocket.isAlive) throw RuntimeException("Connection closed!")

        val ping = Ping(peerId = peerId)
        val dhtObj = DhtObj(DHT_PING, ping)

        outToServer.write(dhtObj.generate())
        outToServer.flush()

        return@async responses.waitFor({
            it is DhtObj && it.obj is Pong && ping.token == it.obj.token
        }, PING_DELAY).await()
    }

    private fun seekResponse() {
        val size = inFromServer.readShort()
        val message = inFromServer.readShort()

        when (message) {
            DHT_SUCCESS -> {
                val dhtSuccess = DhtSuccess.parse(inFromServer)

                Logger.i("[${clientSocket.inetAddress}][DHT_SUCCESS] $dhtSuccess")

                responses.add(dhtSuccess)
            }
            DHT_FAILURE -> {
                val dhtFailure = DhtFailure.parse(inFromServer)

                Logger.e("[${clientSocket.inetAddress}][DHT_FAILURE] $dhtFailure")

                responses.add(dhtFailure)
            }
            DHT_OBJ -> {
                val dhtObj = DhtObj.parse(inFromServer)

                Logger.i("[${clientSocket.inetAddress}][DHT_OBJ] DhtObj: $dhtObj")

                responses.add(dhtObj)
            }
            else -> {
                Logger.e("Invalid message received: $message")
            }
        }
    }

    fun connect() = async {
        if (setupConnection().await()) {
            Logger.i("CLIENT: Setup connection: complete!")
            return@async true
        } else {
            Logger.e("CLIENT: Setup connection: failed!")
            return@async false
        }
    }

    fun send(dhtPut: DhtMessage, filter: (DhtMessage) -> Boolean, maxDelay: Int) = async {
        outToServer.write(dhtPut.generate())
        outToServer.flush()

        return@async responses.waitFor(filter, maxDelay).await()
    }
}