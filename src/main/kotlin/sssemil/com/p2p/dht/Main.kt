package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.runBlocking
import sssemil.com.p2p.dht.util.Logger
import java.net.InetAddress
import java.net.UnknownHostException
import java.util.*

fun main(args: Array<String>) {
    val random = Random()
    val port = 2000 + random.nextInt(100)

    runBlocking {
        val server = Server(port)
        server.start().await()

        val selfClient = Client(InetAddress.getLocalHost(), port)
        selfClient.connect()

        var line: String?

        // let's talk with user
        while (true) {
            println("Enter command (ex. help):")
            line = readLine()

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
                                        Logger.i("ping ${pingIp.hostAddress} $pingPort : ${client.ping().await()}")
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
                            printListHelp()
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

fun printExitHelp() {
    Logger.i("exit: 'exit'\n" +
            "   Exit the program.")
}