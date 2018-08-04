package sssemil.com.p2p.dht.api

import sssemil.com.p2p.dht.util.toBytes
import sssemil.com.p2p.dht.util.toHexString
import java.io.DataInputStream

class DhtGet(val key: ByteArray) : DhtMessage {

    init {
        if (key.size != KEY_LENGTH) {
            throw RuntimeException("key.size = ${key.size} != $KEY_LENGTH")
        }
    }

    override fun generate(destinationPublicKey: ByteArray): ByteArray {
        val sizeInBytes: Short = (4 + KEY_LENGTH).toShort()
        val byteArray = ByteArray(sizeInBytes.toInt())
        var index = 0

        sizeInBytes.toBytes().map { byteArray[index++] = it }

        DHT_GET.toBytes().map { byteArray[index++] = it }

        key.map { byteArray[index++] = it }

        return byteArray
    }

    override fun toString(): String {
        return "DhtGet(key=${key.toHexString()})"
    }

    companion object {
        fun parse(dataInputStream: DataInputStream): DhtGet {
            val key = ByteArray(KEY_LENGTH)
            dataInputStream.read(key)

            return DhtGet(key)
        }
    }
}
