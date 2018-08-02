package sssemil.com.p2p.dht.api

import com.google.gson.Gson
import sssemil.com.p2p.dht.api.model.*
import sssemil.com.p2p.dht.util.toBytes
import java.io.DataInputStream


data class DhtObj(val code: Int, val obj: TokenModel) : DhtMessage {

    override fun generate(): ByteArray {
        val gson = Gson()
        val json = gson.toJson(obj)

        val sizeInBytes: Short = (4 + 4 + json.length).toShort()
        val byteArray = ByteArray(sizeInBytes.toInt())
        var index = 0

        sizeInBytes.toBytes().map { byteArray[index++] = it }

        DHT_OBJ.toBytes().map { byteArray[index++] = it }

        code.toBytes().map { byteArray[index++] = it }

        json.toByteArray().map { byteArray[index++] = it }

        return byteArray
    }

    companion object {
        fun parse(dataInputStream: DataInputStream): DhtObj {
            val gson = Gson()

            val objCode = dataInputStream.readInt()

            val objClass = when (objCode) {
                OBJ_PING -> Ping::class.java
                OBJ_PONG -> Pong::class.java
                OBJ_PUT -> Put::class.java
                OBJ_FIND_VALUE -> FindValue::class.java
                OBJ_FOUND_VALUE -> FoundValue::class.java
                OBJ_FOUND_PEERS -> FoundPeers::class.java
                else -> TokenModel::class.java
            }

            val jsonBytes = ByteArray(dataInputStream.available())
            dataInputStream.read(jsonBytes)

            return DhtObj(objCode, gson.fromJson<TokenModel>(String(jsonBytes), objClass))
        }
    }
}
