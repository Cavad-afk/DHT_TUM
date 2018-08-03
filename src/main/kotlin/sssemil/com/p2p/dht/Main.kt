package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking
import sssemil.com.p2p.dht.api.*
import sssemil.com.p2p.dht.util.Logger
import sssemil.com.p2p.dht.util.generateKey
import sssemil.com.p2p.dht.util.hexStringToByteArray
import sssemil.com.p2p.dht.util.toHexString
import java.io.File
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*

/**
 * Main entry point with simple command line stuff.
 */
fun main(args: Array<String>) {
    val scanner = if (args.isNotEmpty()) {
        Scanner(File(args[0]))
    } else {
        Scanner(System.`in`)
    }

    val random = Random()

    runBlocking {
        val server = Server()
        server.start().await()

        val selfClient = Client(InetAddress.getLocalHost(), server.port)
        selfClient.connect()

        Janitor(server, selfClient).start()

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
                            ping(parts, server)
                        }
                        "list" -> {
                            list(server)
                        }
                        "addPeer" -> {
                            addPeer(parts, server)
                        }
                        "put" -> {
                            put(parts, selfClient)
                        }
                        "get" -> {
                            get(parts, selfClient)
                        }
                        "port" -> {
                            port(parts, server)
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

fun port(parts: List<String>, server: Server) = async {
    if (parts.size == 2) {
        try {
            val newPort = parts[1].toInt()

            server.port = newPort
            server.stop().await()
            server.start().await()
        } catch (e: NumberFormatException) {
            Logger.e(e.localizedMessage)
            printPortHelp()
        }
    } else {
        Logger.i("Current port: $server.port")
    }
}

fun get(parts: List<String>, selfClient: Client) = async {
    if (parts.size == 2) {
        val key = parts[1]

        Logger.i("FindValue key: $key. Searching...")

        val dhtGet = DhtGet(Base64.getDecoder().decode(key))

        val response = selfClient.send(dhtGet,
                {
                    it is DhtSuccess || it is DhtFailure
                },
                DEFAULT_DELAY).await()

        Logger.i("FindValue response: $response")
    } else {
        printGetHelp()
    }
}

fun put(parts: List<String>, client: Client) {
    if (parts.size > 1) {
        val value = parts.joinToString(" ").toByteArray()
        val key = generateKey(value)

        Logger.i("Here is your key: ${key.toHexString()}. Attempting saving.")

        val dhtPut = DhtPut(DEFAULT_TTL, DEFAULT_REPLICATION, key, value)
        client.send(dhtPut)
    } else {
        printPutHelp()
    }
}

fun addPeer(parts: List<String>, server: Server) = async {
    if (parts.size == 3 || parts.size == 4) {
        try {
            val peerIp = InetAddress.getByName(parts[1])
            val peerPort = parts[2].toInt()

            if (peerPort < 2000 || peerPort > 2100) {
                throw NumberFormatException()
            }

            val providedId = if (parts.size == 4) parts[3].hexStringToByteArray() else null

            server.pair(peerIp, peerPort, providedId)
        } catch (e: NumberFormatException) {
            Logger.e("Invalid port number!")
        } catch (e: UnknownHostException) {
            Logger.e("Invalid IP!")
        }
    } else {
        printAddPeerHelp()
    }
}

fun list(server: Server) {
    server.peersStorage.peers.forEachIndexed { i, bucket ->
        bucket.forEachIndexed { j, peer ->
            Logger.i("$i $j $peer")
        }
    }
}

fun ping(parts: List<String>, server: Server) = async {
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
                Logger.i("ping ${pingIp.hostAddress} $pingPort : ${client.ping(server.peerId, server.port).await()}")
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

fun printHelp() {
    printPingHelp()
    printListHelp()
    printAddPeerHelp()
    printPutHelp()
    printGetHelp()
    printPortHelp()
    printExitHelp()
}

fun printPingHelp() {
    Logger.i("ping: 'ping {destination IP} {destination port}'\n" +
            "   Check if there is an open port with our software.")
}

fun printListHelp() {
    Logger.i("list: 'list'\n" +
            "   List peers.")
}

fun printAddPeerHelp() {
    Logger.i("addPeer: 'addPeer {destination IP} {destination port} [optional]{remote peer ID}'\n" +
            "   Add new peer.")
}

fun printPutHelp() {
    Logger.i("put: 'put {value}'\n" +
            "   Add new item. Key will be printed after executing.")
}

fun printGetHelp() {
    Logger.i("get: 'get {key}'\n" +
            "   Get item by key.")
}

fun printPortHelp() {
    Logger.i("port: 'port'\n" +
            "   Print current port number.")
    Logger.i("port: 'port {new port [2000-2100]}'\n" +
            "   Change port number. Will restart the server.")
}

fun printExitHelp() {
    Logger.i("exit: 'exit'\n" +
            "   Exit the program.")
}
