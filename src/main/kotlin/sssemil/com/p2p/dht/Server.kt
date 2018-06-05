package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.async
import sssemil.com.p2p.dht.api.*
import sssemil.com.p2p.dht.util.Logger
import sssemil.com.p2p.dht.util.writeShort
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket

class Server(private val port: Int) {

    private var workersNumber = Runtime.getRuntime().availableProcessors()

    fun start() = async {
        Logger.i("Number of workers: $workersNumber")

        val serverSocket = ServerSocket(port)

        while (true) {
            serverSocket.accept()?.let {
                handleClient(it)
            }
        }
    }

    private fun handleClient(connectionSocket: Socket) = async {
        val inFromClient = DataInputStream(connectionSocket.getInputStream().buffered())
        val outToClient = DataOutputStream(connectionSocket.getOutputStream())

        val size = inFromClient.readShort()
        val message = inFromClient.readShort()

        when (message) {
            DHT_PUT -> {
                val dhtPut = DhtPut.parse(size, inFromClient)

                Logger.i("[${connectionSocket.inetAddress}][DHT_PUT] DhtPut: $dhtPut")

                Storage.store(dhtPut.key, dhtPut.value)

                //TODO send it to reserves
            }
            DHT_GET -> {
                val dhtGet = DhtGet.parse(inFromClient)

                Logger.i("[${connectionSocket.inetAddress}][DHT_GET] DhtGet: $dhtGet")

                Storage.get(dhtGet.key)?.let {
                    sendSuccess(outToClient, dhtGet.key, it)
                    return@async
                }

                //TODO pass along with DHT_GET_INTERNAL and wait max TTL

                sendFailure(outToClient, dhtGet.key)
            }
            DHT_GET_INTERNAL -> {
                val dhtGetInternal = DhtGetInternal.parse(inFromClient)

                Logger.i("[${connectionSocket.inetAddress}][DHT_GET_INTERNAL] DhtGetInternal: $dhtGetInternal")

                Storage.get(dhtGetInternal.key)?.let {
                    sendSuccess(outToClient, dhtGetInternal.key, it)
                    return@async
                }

                //TODO pass along with DHT_GET_INTERNAL and wait TTL - 1
            }
            else -> {
                Logger.e("[${connectionSocket.inetAddress}] Invalid message code: $message!")
            }
        }
    }

    private fun sendFailure(outToClient: DataOutputStream, key: ByteArray) {
        val size = 4 + key.size
        val message = DHT_FAILURE

        Logger.i("[DHT_FAILURE] sending: size: $size, message: $message, key: ${String(key)}")

        outToClient.writeShort(size)
        outToClient.writeShort(message)
        outToClient.write(key)
        outToClient.flush()
    }

    private fun sendSuccess(outToClient: DataOutputStream, key: ByteArray, value: ByteArray) {
        val size = 4 + key.size + value.size
        val message = DHT_SUCCESS

        Logger.i("[DHT_SUCCESS] sending: size: $size, message: $message, key: ${String(key)}")

        outToClient.writeShort(size)
        outToClient.writeShort(message)
        outToClient.write(key)
        outToClient.write(value)
        outToClient.flush()
    }

    companion object {

        private fun bytesToHex(hash: ByteArray): String {
            val hexString = StringBuffer()
            for (i in hash.indices) {
                val hex = Integer.toHexString(0xff.and(hash[i].toInt()))
                if (hex.length == 1) hexString.append('0')
                hexString.append(hex)
            }
            return hexString.toString()
        }
    }
}

private val Socket.isAlive: Boolean
    get() {
        return isBound && isConnected && !isClosed && !isInputShutdown && !isOutputShutdown
    }