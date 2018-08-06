package sssemil.com.p2p.dht.api

import sssemil.com.p2p.dht.util.toBytes
import sssemil.com.p2p.dht.util.toHexString
import java.io.DataInputStream
import java.util.*

class DhtFailure(val key: ByteArray) : DhtMessage {

    init {
        if (key.size != KEY_LENGTH) {
            throw RuntimeException("key.size = ${key.size} != $KEY_LENGTH")
        }
    }

    override fun generate(destinationPublicKey: ByteArray?): ByteArray {
        val sizeInBytes: Short = (4 + KEY_LENGTH).toShort()
        val byteArray = ByteArray(sizeInBytes.toInt())
        var index = 0

        sizeInBytes.toBytes().map { byteArray[index++] = it }

        DHT_FAILURE.toBytes().map { byteArray[index++] = it }

        key.map { byteArray[index++] = it }

        return byteArray
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DhtFailure

        if (!Arrays.equals(key, other.key)) return false

        return true
    }

    override fun hashCode(): Int = Arrays.hashCode(key)

    override fun toString(): String {
        return "DhtFailure(key=${key.toHexString()})"
    }

    companion object {
        fun parse(dataInputStream: DataInputStream): DhtFailure {
            val key = ByteArray(KEY_LENGTH)
            dataInputStream.read(key)

            return DhtFailure(key)
        }
    }
}
