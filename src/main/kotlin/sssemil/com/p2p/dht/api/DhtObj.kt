package sssemil.com.p2p.dht.api

import com.google.gson.Gson
import sssemil.com.p2p.dht.api.model.*
import sssemil.com.p2p.dht.util.KeyPair
import sssemil.com.p2p.dht.util.toBytes
import java.io.DataInputStream


data class DhtObj(val obj: TokenModel) : DhtMessage {

    val code = TokenModel.objectCode(obj.javaClass)

    override fun generate(destinationPublicKey: ByteArray): ByteArray {
        val gson = Gson()
        val json = if (TokenModel.secure(code)) {
            KeyPair.encrypt(destinationPublicKey, gson.toJson(obj).toByteArray())
        } else {
            gson.toJson(obj).toByteArray()
        }

        val sizeInBytes: Short = (4 + 4 + json.size).toShort()
        val byteArray = ByteArray(sizeInBytes.toInt())
        var index = 0

        sizeInBytes.toBytes().map { byteArray[index++] = it }

        DHT_OBJ.toBytes().map { byteArray[index++] = it }

        code.toBytes().map { byteArray[index++] = it }

        json.map { byteArray[index++] = it }

        return byteArray
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DhtObj

        if (code != other.code) return false
        if (obj != other.obj) return false

        return true
    }

    override fun hashCode(): Int {
        var result = code
        result = 31 * result + obj.hashCode()
        return result
    }

    override fun toString(): String {
        return "DhtObj(code=$code, obj=$obj)"
    }

    companion object {
        fun parse(dataInputStream: DataInputStream, receiverKeyPair: KeyPair): DhtObj {
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

            val encryptedJsonBytes = ByteArray(dataInputStream.available())
            dataInputStream.read(encryptedJsonBytes)

            val jsonBytes = if (TokenModel.secure(objCode)) {
                KeyPair.decrypt(receiverKeyPair.privateKey, encryptedJsonBytes)
            } else {
                encryptedJsonBytes
            }

            return DhtObj(gson.fromJson<TokenModel>(String(jsonBytes), objClass))
        }
    }
}
