package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.runBlocking
import sssemil.com.p2p.dht.api.*
import sssemil.com.p2p.dht.api.model.Pong
import sssemil.com.p2p.dht.util.Logger
import sssemil.com.p2p.dht.util.generateId
import sssemil.com.p2p.dht.util.generateKey
import sssemil.com.p2p.dht.util.toBase64
import java.io.File
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*

fun main(args: Array<String>) {
    val scanner = if (args.isNotEmpty()) {
        Scanner(File(args[0]))
    } else {
        Scanner(System.`in`)
    }

    val random = Random()
    val port = 2000 + random.nextInt(100)
    val thisPeerId = generateId()

    runBlocking {
        val server = Server(port, thisPeerId)
        server.start().await()

        val selfClient = Client(InetAddress.getLocalHost(), port)
        selfClient.connect()

        var line: String?

        // let's talk with user
        while (true) {
            Logger.i("Enter command (ex. help):")
            line = scanner.nextLine()

            if (line != null) {
                val parts = line.split(" ")

                if (parts.isNotEmpty()) {
                    when (parts[0]) {
                        "help" -> {
                            printHelp()
                        }
                        "ping" -> {
                            if (parts.size == 3) {
                                try {
                                    val pingIp = InetAddress.getByName(parts[1])
                                    val pingPort = parts[2].toInt()
                                    if (pingPort < 2000 || pingPort > 2100) {
                                        throw NumberFormatException()
                                    }

                                    val client = Client(pingIp, pingPort)

                                    if (!client.connect().await()) {
                                        Logger.i("Connection failed!")
                                    } else {
                                        Logger.i("ping ${pingIp.hostAddress} $pingPort : ${client.ping(thisPeerId).await()}")
                                    }
                                } catch (e: NumberFormatException) {
                                    Logger.e("Invalid port number!")
                                } catch (e: UnknownHostException) {
                                    Logger.e("Invalid IP!")
                                }
                            } else {
                                printPingHelp()
                            }
                        }
                        "list" -> {
                            server.peersStorage.forEachIndexed { i, bucket ->
                                bucket.forEachIndexed { j, peer ->
                                    Logger.i("$i $j $peer")
                                }
                            }
                        }
                        "addPeer" -> {
                            if (parts.size == 3 || parts.size == 4) {
                                try {
                                    val peerIp = InetAddress.getByName(parts[1])
                                    val peerPort = parts[2].toInt()
                                    if (peerPort < 2000 || peerPort > 2100) {
                                        throw NumberFormatException()
                                    }

                                    val client = Client(peerIp, peerPort)

                                    if (!client.connect().await()) {
                                        Logger.i("Connection failed!")
                                    } else {
                                        val pong = client.ping(thisPeerId).await() as DhtObj?
                                        Logger.i("ping ${peerIp.hostAddress} $peerPort : $pong")

                                        pong?.let {
                                            var valid = true
                                            val factualPeerId = (it.obj as Pong).peerId
                                            if (parts.size == 4) {
                                                val providedId = Base64.getDecoder().decode(parts[3])
                                                providedId?.let { providedId1 ->
                                                    if (!providedId1.contentEquals(factualPeerId)) {
                                                        Logger.e("Peer verification failed!")
                                                        valid = false
                                                    }
                                                }
                                            }

                                            if (valid) {
                                                val peer = Peer(factualPeerId, peerIp, peerPort)
                                                Logger.i("Adding new peer: $peer")
                                                server.addPeer(peer)
                                            }
                                        }
                                    }
                                } catch (e: NumberFormatException) {
                                    Logger.e("Invalid port number!")
                                } catch (e: UnknownHostException) {
                                    Logger.e("Invalid IP!")
                                }
                            } else {
                                printAddPeerHelp()
                            }
                        }
                        "put" -> {
                            if (parts.size > 1) {
                                val value = line.substring(line.indexOf(" ") + 1).toByteArray()
                                val key = generateKey(value)

                                Logger.i("Here is your key: ${key.toBase64()}. Attempting saving.")

                                val dhtPut = DhtPut(15, 15, key, value)
                                selfClient.send(dhtPut, { true }, 0).await()
                            }
                        }
                        "get" -> {
                            if (parts.size == 1) {
                                val key = parts[1]

                                Logger.i("Get key: $key. Searching...")

                                val dhtGet = DhtGet(Base64.getDecoder().decode(key))

                                val response = selfClient.send(dhtGet,
                                        {
                                            it is DhtSuccess || it is DhtFailure
                                        },
                                        DEFAULT_DELAY).await()

                                Logger.i("Get response: $response")
                            }
                        }
                        "exit" -> {
                            System.exit(0)
                        }
                    }
                }
            }
        }
    }
}


fun printHelp() {
    printPingHelp()
    printListHelp()
    printAddPeerHelp()
    printExitHelp()
}

fun printAddPeerHelp() {
    Logger.i("addPeer: 'addPeer {destination IP} {destination port} [optional]{remote peer ID}'\n" +
            "   Add new peer.")
}

fun printPingHelp() {
    Logger.i("ping: 'ping {destination IP} {destination port}'\n" +
            "   Check if there is an open port with our software.")
}

fun printListHelp() {
    Logger.i("list: 'list'\n" +
            "   List peers.")
}

fun printExitHelp() {
    Logger.i("exit: 'exit'\n" +
            "   Exit the program.")
}
