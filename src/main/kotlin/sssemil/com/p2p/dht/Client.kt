package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.async
import sssemil.com.p2p.dht.api.*
import sssemil.com.p2p.dht.api.model.Ping
import sssemil.com.p2p.dht.util.ActiveList
import sssemil.com.p2p.dht.util.KeyPair
import sssemil.com.p2p.dht.util.Logger
import sssemil.com.p2p.dht.util.isAlive
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Thread.sleep
import java.net.ConnectException
import java.net.InetAddress
import java.net.Socket
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class Client(private val serverAddress: InetAddress, val serverPort: Int, private val localKeyPair: KeyPair) {
    private lateinit var clientSocket: Socket
    private lateinit var outToServer: DataOutputStream
    private lateinit var inFromServer: DataInputStream

    private val run = AtomicBoolean(true)

    private val responses = ActiveList<DhtMessage>()

    private fun setupConnection(): Boolean {
        try {
            clientSocket = Socket(serverAddress, serverPort)
            outToServer = DataOutputStream(clientSocket.getOutputStream())
            inFromServer = DataInputStream(clientSocket.getInputStream())

            thread {
                monitor()
            }

            return true
        } catch (e: ConnectException) {
            Logger.e(e.localizedMessage)
        }

        return false
    }

    private fun monitor() {
        while (clientSocket.isAlive && run.get()) {
            if (inFromServer.available() > 0) {
                seekResponse()
            }

            sleep(10)
        }

        Logger.w("Socket closed!")
    }

    fun stop() {
        run.set(false)
        sleep(150)
    }

    fun ping(localPeerId: ByteArray, port: Int) = async {
        if (!clientSocket.isAlive) throw RuntimeException("Connection closed!")

        val ping = Ping(localPeerId, port)
        val dhtObj = DhtObj(ping)

        return@async send(dhtObj, PING_DELAY, null)
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
                val dhtObj = DhtObj.parse(inFromServer, localKeyPair)

                Logger.i("[${clientSocket.inetAddress}][DHT_OBJ] DhtObj: $dhtObj")

                responses.add(dhtObj)
            }
            else -> {
                Logger.e("Invalid message received: $message")
            }
        }
    }

    fun connect(): Boolean {
        return if (setupConnection()) {
            Logger.d("[CLIENT] Setup connection: complete!")
            true
        } else {
            Logger.e("[CLIENT] Setup connection failed!")
            false
        }
    }

    fun send(data: ByteArray) {
        outToServer.write(data)
        outToServer.flush()
    }

    fun send(dhtMessage: DhtMessage, destinationPublicKey: ByteArray?) {
        send(dhtMessage.generate(destinationPublicKey))
    }

    suspend fun send(dhtObj: DhtObj, maxDelay: Long, destinationPublicKey: ByteArray?) = send(dhtObj, {
        val accept = it is DhtObj && it.obj.token == dhtObj.obj.token
        Logger.d("Got $it for $dhtObj. Accept: $accept")
        accept
    }, maxDelay, destinationPublicKey)

    suspend fun send(dhtMessage: DhtMessage, filter: (DhtMessage) -> Boolean, maxDelay: Long, destinationPublicKey: ByteArray?): DhtMessage? {
        send(dhtMessage, destinationPublicKey)
        return responses.waitFor(filter, maxDelay).await()
    }
}