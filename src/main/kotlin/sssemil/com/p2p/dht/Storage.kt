package sssemil.com.p2p.dht

import java.util.*

class Storage {

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

    companion object {
        private val hashMap = HashMap<ByteArray, Entry>()

        fun store(key: ByteArray, value: ByteArray, ttl: Long) {
            hashMap[key] = Entry(value, ttl, System.currentTimeMillis())
        }

        fun remove(key: ByteArray): Boolean {
            if (contains(key)) {
                hashMap.remove(key)

                return true
            }

            return false
        }

        fun contains(key: ByteArray) = hashMap.contains(key)

        fun get(key: ByteArray): ByteArray? {
            cleanup()

            return if (contains(key)) {
                hashMap[key]?.value
            } else null
        }

        fun cleanup() {
            val keys = hashMap.keys
            keys.forEach { key ->
                if (hashMap[key]?.isOutdated() == true) {
                    hashMap.remove(key)
                }
            }
        }

        fun getAll() = hashMap
    }
}