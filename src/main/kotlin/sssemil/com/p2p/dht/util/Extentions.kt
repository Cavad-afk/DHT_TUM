package sssemil.com.p2p.dht.util

import java.io.DataOutputStream
import java.net.Socket
import java.nio.charset.Charset
import java.util.*

fun DataOutputStream.writeShort(value: Short) {
    writeShort(value.toInt())
}

fun Short.toBytes(): ByteArray {
    val result = ByteArray(2)
    result[1] = toByte()
    result[0] = toInt().shr(8).toByte()
    return result
}

fun Int.toBytes(): ByteArray {
    val result = ByteArray(4)
    result[3] = toByte()
    result[2] = shr(8).toByte()
    result[1] = shr(16).toByte()
    result[0] = shr(24).toByte()
    return result
}

val Socket.isAlive: Boolean
    get() {
        return isBound && isConnected && !isClosed && !isInputShutdown && !isOutputShutdown
    }


fun ByteArray.str(): String {
    return Base64.getDecoder().decode(this).toString(Charset.defaultCharset())
}

fun ByteArray.toBase64(): String {
    return Base64.getEncoder().encode(this).toString(Charset.defaultCharset())
}