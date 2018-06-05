package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.async
import sssemil.com.p2p.dht.api.*
import sssemil.com.p2p.dht.util.Logger
import sssemil.com.p2p.dht.util.writeShort
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ConnectException
import java.net.Socket
import java.util.*

class Client(private val serverAddress: String, val serverPort: Int) {
    private lateinit var clientSocket: Socket
    private lateinit var outToServer: DataOutputStream
    private lateinit var inFromServer: DataInputStream

    private fun setupConnection() {
        try {
            clientSocket = Socket(serverAddress, serverPort)
            outToServer = DataOutputStream(clientSocket.getOutputStream())
            inFromServer = DataInputStream(clientSocket.getInputStream())
        } catch (e: ConnectException) {
            setupConnection()
        }
    }

    fun putValue(key: ByteArray, value: ByteArray) {
        if (key.size != 32) throw RuntimeException("Key length has to be 32!")

        var size = 4
        val message = DHT_PUT

        size += key.size + value.size

        Logger.i("[DHT_PUT] Sending: size: $size, message: $message, key: ${String(key)}, value: $value")

        outToServer.writeShort(size)
        outToServer.writeShort(message)
        outToServer.write(key)
        outToServer.write(value)
        outToServer.flush()
    }

    fun getValue(key: ByteArray) = async {
        if (key.size != 32) throw RuntimeException("Key length has to be 32!")

        var size = 4
        val message = DHT_GET

        size += key.size

        Logger.i("[DHT_GET] Sending: size: $size, message: $message, key: ${String(key)}")

        outToServer.writeShort(size)
        outToServer.writeShort(message)
        outToServer.write(key)
        outToServer.flush()

        return@async getResponse(inFromServer)
    }

    private fun getResponse(inFromServer: DataInputStream): Response {
        val size = inFromServer.readShort()
        val message = inFromServer.readShort()

        when (message) {
            DHT_SUCCESS -> {
                val key = ByteArray(KEY_LENGTH)
                inFromServer.read(key)
                val value = ByteArray(size - 4 - key.size)
                inFromServer.read(value)

                Logger.i("[DHT_SUCCESS] size: $size, message: $message, key: ${String(key)}, value: ${String(value)}")

                return Response(message, value)
            }
            DHT_FAILURE -> {
                val key = ByteArray(KEY_LENGTH)
                inFromServer.read(key)

                Logger.e("[DHT_FAILURE] size: $size, message: $message, key: ${String(key)}")
                return Response(message)
            }
            else -> {
                Logger.e("Invalid message received: $message")
                return Response(message)
            }
        }
    }

    data class Response(val message: Short, val value: ByteArray = byteArrayOf()) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Response

            if (message != other.message) return false
            if (!Arrays.equals(value, other.value)) return false

            return true
        }

        override fun hashCode(): Int {
            var result: Int = message.toInt()
            result = 31 * result + Arrays.hashCode(value)
            return result
        }
    }

    init {
        setupConnection()
    }
}