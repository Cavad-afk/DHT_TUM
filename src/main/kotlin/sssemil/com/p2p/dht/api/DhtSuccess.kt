package sssemil.com.p2p.dht.api

import sssemil.com.p2p.dht.util.toBytes
import sssemil.com.p2p.dht.util.toHexString
import java.io.DataInputStream
import java.util.*

class DhtSuccess(val key: ByteArray, val value: ByteArray) : DhtMessage {

    init {
        if (key.size != KEY_LENGTH) {
            throw RuntimeException("key.size = ${key.size} != $KEY_LENGTH")
        }
    }

    override fun generate(destinationPublicKey: ByteArray?): ByteArray {
        val sizeInBytes: Short = (4 + KEY_LENGTH + value.size).toShort()
        val byteArray = ByteArray(sizeInBytes.toInt())
        var index = 0

        sizeInBytes.toBytes().map { byteArray[index++] = it }

        DHT_SUCCESS.toBytes().map { byteArray[index++] = it }

        key.map { byteArray[index++] = it }

        value.map { byteArray[index++] = it }

        return byteArray
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DhtSuccess

        if (!Arrays.equals(key, other.key)) return false
        if (!Arrays.equals(value, other.value)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(key)
        result = 31 * result + Arrays.hashCode(value)
        return result
    }

    override fun toString(): String {
        return "DhtSuccess(key=${key.toHexString()}, value=${value.toHexString()})"
    }

    companion object {
        fun parse(dataInputStream: DataInputStream): DhtSuccess {
            val key = ByteArray(KEY_LENGTH)
            dataInputStream.read(key)

            val value = ByteArray(dataInputStream.available())
            dataInputStream.read(value)

            return DhtSuccess(key, value)
        }
    }
}
