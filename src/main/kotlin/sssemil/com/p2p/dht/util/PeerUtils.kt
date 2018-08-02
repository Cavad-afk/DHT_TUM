package sssemil.com.p2p.dht.util

import org.apache.commons.codec.digest.DigestUtils
import sssemil.com.p2p.dht.api.KEY_LENGTH
import java.util.*


/**
 * Use it to generate random IDs for peers.
 */
fun generateId(): ByteArray {
    val random = Random()

    var id = ByteArray(KEY_LENGTH)
    random.nextBytes(id)

    return DigestUtils.md5(id)
}