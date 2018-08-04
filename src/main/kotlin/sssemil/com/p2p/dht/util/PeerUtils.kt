package sssemil.com.p2p.dht.util

import org.apache.commons.codec.digest.DigestUtils
import sssemil.com.p2p.dht.api.RSA_KEY_LENGTH
import java.security.KeyPairGenerator

/**
 * Use it to generate RSA key pair.
 */
fun generateKeyPair(): KeyPair {
    val keyGen: KeyPairGenerator = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(RSA_KEY_LENGTH)
    val pair = keyGen.generateKeyPair()
            ?: throw RuntimeException("Something went very wrong with key pair generation...")

    return KeyPair(pair.private.encoded, pair.public.encoded)
}

/**
 * Use it to generate key for data
 */
fun generateKey(data: ByteArray) = DigestUtils.md5(data)