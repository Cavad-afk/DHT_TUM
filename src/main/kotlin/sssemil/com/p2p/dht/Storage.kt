package sssemil.com.p2p.dht

class Storage {

    companion object {
        private val hashMap = HashMap<ByteArray, ByteArray>()

        fun store(key: ByteArray, value: ByteArray, ttl: Short) {
            hashMap[key] = value
        }

        fun remove(key: ByteArray): Boolean {
            if (contains(key)) {
                hashMap.remove(key)

                return true
            }

            return false
        }

        fun contains(key: ByteArray) = hashMap.contains(key)

        fun get(key: ByteArray) = if (contains(key)) {
            hashMap[key]
        } else null
    }
}