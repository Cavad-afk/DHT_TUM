package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import sssemil.com.p2p.dht.api.DEFAULT_REPLICATION
import sssemil.com.p2p.dht.api.DhtObj
import sssemil.com.p2p.dht.api.model.Put
import sssemil.com.p2p.dht.util.Logger
import sssemil.com.p2p.dht.util.hexStringToByteArray

/**
 * Janitor performs repeating maintenance tasks.
 */
class Janitor(private val server: Server, private val client: Client) {
    fun start() = async {
        delay(MINUTE)

        server.storage.cleanup()

        val all = server.storage.getAll()

        Logger.i("[JANITOR] Resending all data.")

        all.forEach { key, value ->
            server.peersStorage.findClosest(key.hexStringToByteArray(), hashSetOf()).forEach { dest ->
                val dhtObj = DhtObj(Put(
                        value.ttl - (System.currentTimeMillis() - value.arrivedAt),
                        DEFAULT_REPLICATION, value.value))
                if ((dhtObj.obj as Put).ttl > 0) {
                    val destClient = Client(dest.peer.ip, dest.peer.port, server.peersStorage.id)
                    destClient.connect()
                    destClient.send(dhtObj, dest.peer.id)
                }
            }
        }

        Logger.i("[JANITOR] Resending all data - completed.")

        delay(HOUR)
    }

    companion object {
        const val SECOND = 1000L
        const val MINUTE = 60 * SECOND
        const val HOUR = 60 * MINUTE
        const val DAY = 24 * HOUR
    }
}