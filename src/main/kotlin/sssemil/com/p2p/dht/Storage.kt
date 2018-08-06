package sssemil.com.p2p.dht

import sssemil.com.p2p.dht.util.Logger
import sssemil.com.p2p.dht.util.toHexString
import java.util.*

class Storage {

    private val hashMap = HashMap<String, Entry>()

    fun store(key: ByteArray, value: ByteArray, ttl: Long) {
        synchronized(hashMap) {
            hashMap[key.toHexString()] = Entry(value, ttl, System.currentTimeMillis())
        }
    }

    fun contains(key: ByteArray): Boolean {
        synchronized(hashMap) {
            return hashMap.containsKey(key.toHexString())
        }
    }

    fun get(key: ByteArray): ByteArray? {
        cleanup()

        synchronized(hashMap) {
            return if (hashMap.containsKey(key.toHexString())) {
                hashMap[key.toHexString()]?.value
            } else null
        }
    }

    fun cleanup() {
        synchronized(hashMap) {
            val keys = hashMap.keys.toList()
            keys.forEach { key ->
                if (hashMap[key]?.isOutdated() == true) {
                    hashMap.remove(key)
                    Logger.d("[STORAGE] Removing outdated item: $key")
                }
            }
        }
    }

    fun getAll() = hashMap

    data class Entry(val value: ByteArray, val ttl: Long, val arrivedAt: Long) {
        fun isOutdated() = System.currentTimeMillis() - arrivedAt > ttl

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as Entry

            if (!Arrays.equals(value, other.value)) return false
            if (ttl != other.ttl) return false
            if (arrivedAt != other.arrivedAt) return false

            return true
        }

        override fun hashCode(): Int {
            var result = Arrays.hashCode(value)
            result = (31 * result + ttl).toInt()
            result = 31 * result + arrivedAt.hashCode()
            return result
        }
    }
}