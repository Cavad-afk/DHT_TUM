package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import sssemil.com.p2p.dht.api.*
import sssemil.com.p2p.dht.api.model.Ping
import sssemil.com.p2p.dht.api.model.Pong
import sssemil.com.p2p.dht.util.IdKeyUtils
import sssemil.com.p2p.dht.util.Logger
import sssemil.com.p2p.dht.util.SortedList
import sssemil.com.p2p.dht.util.isAlive
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*

class Server(private val port: Int, private val thisPeerId: ByteArray) {

    private var workersNumber = Runtime.getRuntime().availableProcessors()

    val peersStorage = Array(KEY_LENGTH * 8) { SortedList<PeerHolder>(BUCKETS) }

    init {
        if (thisPeerId.size != KEY_LENGTH) throw RuntimeException("Peer ID has to be this long : $KEY_LENGTH!")
    }

    fun start() = async {
        Logger.i("Number of workers: $workersNumber")
        Logger.i("Port number: $port")
        Logger.i("ID: ${Base64.getEncoder().encode(thisPeerId).toString(Charset.defaultCharset())}")

        val serverSocket = ServerSocket(port)

        async {
            while (true) {
                serverSocket.accept()?.let {
                    handleClient(it)
                }
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

                        Storage.store(dhtPut.key, dhtPut.value, dhtPut.ttl)

                        val distance = IdKeyUtils.distance(dhtPut.key, thisPeerId)
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

                val pong = Pong(ping.token, thisPeerId)

                Logger.i("[DHT_PONG] sending: pong: $pong")

                val reply = DhtObj(DHT_PONG, pong).generate()

                outToClient.write(reply)
            }
        }
    }

    private fun sendFailure(outToClient: DataOutputStream, key: ByteArray) {
        val dhtFailure = DhtFailure(key)

        Logger.i("[DHT_FAILURE] sending: $dhtFailure")

        outToClient.write(dhtFailure.generate())
        outToClient.flush()
    }

    private fun sendSuccess(outToClient: DataOutputStream, key: ByteArray, value: ByteArray) {
        val dhtSuccess = DhtSuccess(key, value)

        Logger.i("[DHT_SUCCESS] sending: $dhtSuccess")

        outToClient.write(dhtSuccess.generate())
        outToClient.flush()
    }

    fun addPeer(peer: Peer) {
        val distance = IdKeyUtils.distance(peer.id, thisPeerId)

        Logger.i("Add peer $peer at $distance")

        peersStorage[distance].add(PeerHolder(peer.id, peer.ip, peer.port,
                System.currentTimeMillis(), System.currentTimeMillis()))
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