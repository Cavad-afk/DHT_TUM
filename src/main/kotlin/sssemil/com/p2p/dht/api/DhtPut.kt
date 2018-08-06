package sssemil.com.p2p.dht.api

import sssemil.com.p2p.dht.util.toBytes
import sssemil.com.p2p.dht.util.toHexString
import java.io.DataInputStream
import java.util.*

class DhtPut(val ttl: Short, val replicationsLeft: Byte, val key: ByteArray, val value: ByteArray) : DhtMessage {

    init {
        if (key.size != KEY_LENGTH) {
            throw RuntimeException("key.size = ${key.size} != $KEY_LENGTH")
        }
    }

    override fun generate(destinationPublicKey: ByteArray?): ByteArray {
        val sizeInBytes: Short = (8 + KEY_LENGTH + value.size).toShort()
        val byteArray = ByteArray(sizeInBytes.toInt())
        var index = 0

        sizeInBytes.toBytes().map { byteArray[index++] = it }

        DHT_PUT.toBytes().map { byteArray[index++] = it }

        ttl.toBytes().map { byteArray[index++] = it }

        byteArray[index++] = replicationsLeft

        index++ //reserved

        key.map { byteArray[index++] = it }

        value.map { byteArray[index++] = it }

        return byteArray
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DhtPut

        if (ttl != other.ttl) return false
        if (replicationsLeft != other.replicationsLeft) return false
        if (!Arrays.equals(key, other.key)) return false
        if (!Arrays.equals(value, other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = ttl.toInt()
        result = 31 * result + replicationsLeft
        result = 31 * result + Arrays.hashCode(key)
        result = 31 * result + Arrays.hashCode(value)
        return result
    }

    override fun toString(): String {
        return "DhtPut(ttl=$ttl, replicationsLeft=$replicationsLeft, key=${key.toHexString()}, value=${value.toHexString()})"
    }

    companion object {
        fun parse(size: Short, dataInputStream: DataInputStream): DhtPut {
            val ttl = dataInputStream.readShort()

            val replicationsLeft = dataInputStream.read().toByte()
            val reserved = dataInputStream.read()

            val key = ByteArray(KEY_LENGTH)
            dataInputStream.read(key)

            val value = ByteArray(dataInputStream.available())
            dataInputStream.read(value)

            return DhtPut(ttl, replicationsLeft, key, value)
        }
    }
}
