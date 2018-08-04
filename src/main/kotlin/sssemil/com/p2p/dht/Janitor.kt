package sssemil.com.p2p.dht

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import sssemil.com.p2p.dht.api.DEFAULT_REPLICATION
import sssemil.com.p2p.dht.api.DhtObj
import sssemil.com.p2p.dht.api.OBJ_PUT
import sssemil.com.p2p.dht.api.model.Put
import sssemil.com.p2p.dht.util.Logger

/**
 * Janitor performs repeating maintenance tasks.
 */
class Janitor(private val server: Server, private val client: Client) {
    fun start() = async {
        delay(MINUTE)

        server.storage.cleanup()

        val all = server.storage.getAll()

        Logger.i("[JANITOR] Resending all data.")

        all.forEach { _, value ->
            val dhtObj = DhtObj(OBJ_PUT, Put(
                    value.ttl - (System.currentTimeMillis() - value.arrivedAt),
                    DEFAULT_REPLICATION, value.value))
            if ((dhtObj.obj as Put).ttl > 0) {
                client.send(dhtObj)
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