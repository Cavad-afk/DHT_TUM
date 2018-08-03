package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import sssemil.com.p2p.dht.api.*
import sssemil.com.p2p.dht.api.model.*
import sssemil.com.p2p.dht.util.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.collections.HashSet

class Server(var port: Int, var thisPeerId: ByteArray) {

    private val run = AtomicBoolean(true)

    private var workersNumber = Runtime.getRuntime().availableProcessors()

    val peersStorage = Array(KEY_LENGTH * 8) { SortedList<PeerHolder>(BUCKETS) }

    init {
        if (thisPeerId.size != KEY_LENGTH) throw RuntimeException("Peer ID has to be this long : $KEY_LENGTH!")
    }

    fun start() = async {
        run.set(true)

        Logger.i("Number of workers: $workersNumber")
        Logger.i("Port number: $port")
        Logger.i("ID: ${Base64.getEncoder().encode(thisPeerId).toString(Charset.defaultCharset())}")

        val serverSocket = ServerSocket(port)

        async {
            while (run.get()) {
                serverSocket.accept()?.let {
                    handleClient(it)
                }
            }
        }
    }

    fun stop() = async {
        run.set(false)
        delay(150)
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

                        val put = Put(dhtPut.ttl, dhtPut.replicationsLeft, dhtPut.value)

                        handleObj(socket, outToClient, DhtObj(OBJ_PUT, put))
                    }
                    DHT_GET -> {
                        val dhtGet = DhtGet.parse(inFromClient)

                        Logger.i("[${socket.inetAddress}][DHT_GET] DhtGet: $dhtGet")

                        val distance = IdKeyUtils.distance(dhtGet.key, thisPeerId)

                        if (distance < TOLERANCE) {
                            Logger.i("Looking for the key-value in local storage!")

                            Storage.get(dhtGet.key)?.let {
                                sendSuccess(outToClient, dhtGet.key, it)
                                return@async
                            }
                        }

                        val sentTo = HashSet<String>()

                        findClosestPeers(dhtGet.key, sentTo).forEach { it ->
                            val client = Client(it.peer.ip, it.peer.port)
                            if (client.connect().await()) {
                                val response = client.send(DhtObj(OBJ_FIND_VALUE, FindValue(dhtGet.key)), DEFAULT_DELAY).await()

                                if (response != null && response is DhtObj) {
                                    when (response.code) {
                                        OBJ_FOUND_VALUE -> {
                                            val foundValue = response.obj as FoundValue

                                            val value = foundValue.value
                                            val key = generateKey(value)

                                            if (dhtGet.key.contentEquals(key)) {
                                                Logger.i("Value found for ${key.toBase64()}!")

                                                sendSuccess(outToClient, key, value)
                                            }
                                        }
                                        OBJ_FOUND_PEERS -> {
                                            val foundPeers = response.obj as FoundPeers

                                            foundPeers.peers.forEach {
                                                val testClient = Client(it.ip, it.port)

                                                if (!testClient.connect().await()) {
                                                    Logger.e("Connection failed to $it!")
                                                } else {
                                                    val pong = testClient.ping(thisPeerId, port).await() as DhtObj?
                                                    Logger.i("ping ${it.ip.hostAddress} ${it.port} : $pong")

                                                    pong?.let { pongObj ->
                                                        var valid = true
                                                        val factualPeerId = (pongObj.obj as Pong).peerId

                                                        val providedId = it.id
                                                        if (!providedId.contentEquals(factualPeerId)) {
                                                            Logger.e("Peer verification failed!")
                                                            valid = false
                                                        }

                                                        if (valid) {
                                                            Logger.i("Adding new peer: $it")
                                                            addPeer(it)
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        else -> {
                                            Logger.w("Hmmm...")
                                        }
                                    }
                                }
                            }
                        }

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

    data class WeightedPeer(val distance: Int, val peer: Peer) : Comparable<WeightedPeer> {
        override fun compareTo(other: WeightedPeer) = distance.compareTo(other.distance)
    }

    private fun findClosestPeers(key: ByteArray, exceptions: HashSet<String>): LinkedList<WeightedPeer> {
        val tmp = SortedList<WeightedPeer>(GRAB_SIZE)
        peersStorage.iterator().forEach { sortedList ->
            sortedList.forEach {
                if (!exceptions.contains(it.id.toBase64())) {
                    tmp.add(WeightedPeer(IdKeyUtils.distance(key, it.id), it))
                }
            }
        }

        return tmp.list;
    }

    private fun handleObj(connectionSocket: Socket, outToClient: DataOutputStream, dhtObj: DhtObj) = async {
        when (dhtObj.code) {
            OBJ_PING -> {
                val ping = dhtObj.obj as Ping

                Logger.i("[${connectionSocket.inetAddress}][OBJ_PING] $ping")

                val pong = Pong(thisPeerId, port)
                pong.token = ping.token

                Logger.i("[OBJ_PONG] sending: pong: $pong")

                val reply = DhtObj(OBJ_PONG, pong).generate()

                outToClient.write(reply)

                addPeer(Peer(ping.peerId, connectionSocket.inetAddress, ping.port))
            }
            OBJ_PUT -> {
                val put = dhtObj.obj as Put

                Logger.i("[${connectionSocket.inetAddress}][OBJ_PUT] $put")

                if (Storage.contains(put.key)) {
                    Logger.i("Ignore key, we have it : ${put.key}")
                }

                val distance = IdKeyUtils.distance(put.key, thisPeerId)

                Logger.i("Distance to key: $distance")

                if (distance < TOLERANCE) {
                    Logger.i("Storing the key-value!")

                    Storage.store(put.key, put.value, put.ttl)

                    put.replicationsLeft--
                }

                val sentTo = HashSet<String>()

                while (put.replicationsLeft > 0) {
                    val closest = findClosestPeers(put.key, sentTo)

                    closest.forEach { it ->
                        if (put.replicationsLeft > 0) {
                            val client = Client(it.peer.ip, it.peer.port)

                            if (client.connect().await()) {
                                put.replicationsLeft--
                                client.send(DhtObj(OBJ_PUT, put), { true }, 0)
                                sentTo.add(it.peer.id.toBase64())
                            }
                        }
                    }

                    if (closest.size < GRAB_SIZE) {
                        break
                    }
                }
            }
            OBJ_FIND_VALUE -> {
                val findValue = dhtObj.obj as FindValue
                val distance = IdKeyUtils.distance(findValue.key, thisPeerId)

                if (distance < TOLERANCE) {
                    Logger.i("Looking for the key-value in local storage!")

                    Storage.get(findValue.key)?.let {
                        outToClient.write(DhtObj(OBJ_FOUND_VALUE, FoundValue(it)).generate())
                        outToClient.flush()
                        return@async
                    }
                }

                val closestPeers = findClosestPeers(findValue.key, hashSetOf()).map { it.peer }

                outToClient.write(DhtObj(OBJ_FOUND_PEERS, FoundPeers(closestPeers.toTypedArray())).generate())
                outToClient.flush()

                return@async
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