package sssemil.com.p2p.dht.api

import sssemil.com.p2p.dht.util.toBytes
import java.io.DataInputStream

data class DhtGet(val key: ByteArray) : DhtMessage {

    override fun generate(): ByteArray {
        val sizeInBytes: Short = (4 + KEY_LENGTH).toShort()
        val byteArray = ByteArray(sizeInBytes.toInt())
        var index = 0

        sizeInBytes.toBytes().map { byteArray[index++] = it }

        DHT_GET.toBytes().map { byteArray[index++] = it }

        key.map { byteArray[index++] = it }

        return byteArray
    }

    companion object {
        fun parse(dataInputStream: DataInputStream): DhtGet {
            val key = ByteArray(KEY_LENGTH)
            dataInputStream.read(key)

            return DhtGet(key)
        }
    }
}
