package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import sssemil.com.p2p.dht.api.*
import sssemil.com.p2p.dht.api.model.Ping
import sssemil.com.p2p.dht.api.model.Pong
import sssemil.com.p2p.dht.util.Logger
import sssemil.com.p2p.dht.util.isAlive
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket

class Server(private val port: Int) {

    private var workersNumber = Runtime.getRuntime().availableProcessors()

    fun start() = async {
        Logger.i("Number of workers: $workersNumber")
        Logger.i("Port number: $port")

        val serverSocket = ServerSocket(port)

        while (true) {
            serverSocket.accept()?.let {
                handleClient(it)
            }
        }
    }

    private fun handleClient(socket: Socket) = async {
        while (socket.isAlive) {
            val inFromClient = DataInputStream(socket.getInputStream().buffered())
            val outToClient = DataOutputStream(socket.getOutputStream())

            if (inFromClient.available() > 0) {
                val size = inFromClient.readShort()
                val message = inFromClient.readShort()

                when (message) {
                    DHT_PUT -> {
                        val dhtPut = DhtPut.parse(size, inFromClient)

                        Logger.i("[${socket.inetAddress}][DHT_PUT] DhtPut: $dhtPut")

                        Storage.store(dhtPut.key, dhtPut.value)

                        //TODO send it to reserves
                    }
                    DHT_GET -> {
                        val dhtGet = DhtGet.parse(inFromClient)

                        Logger.i("[${socket.inetAddress}][DHT_GET] DhtGet: $dhtGet")

                        Storage.get(dhtGet.key)?.let {
                            sendSuccess(outToClient, dhtGet.key, it)
                            return@async
                        }

                        //TODO pass along with DHT_GET_INTERNAL and wait max TTL

                        sendFailure(outToClient, dhtGet.key)
                    }
                    DHT_OBJ -> {
                        val dhtObj = DhtObj.parse(inFromClient)

                        Logger.i("[${socket.inetAddress}][DHT_OBJ] DhtObj: $dhtObj")

                        handleObj(socket, outToClient, dhtObj)
                    }
                    else -> {
                        Logger.e("[${socket.inetAddress}] Invalid message code: $message!")
                    }
                }
            }

            delay(10)
        }
    }

    private fun handleObj(connectionSocket: Socket, outToClient: DataOutputStream, dhtObj: DhtObj) {
        when (dhtObj.code) {
            DHT_PING -> {
                val ping = dhtObj.obj as Ping

                Logger.i("[${connectionSocket.inetAddress}][DHT_PING] $ping")

                val pong = Pong(ping.token)

                Logger.i("[DHT_PONG] sending: pong: $pong")

                val reply = DhtObj(DHT_PONG, pong).generate()

                outToClient.write(reply)
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