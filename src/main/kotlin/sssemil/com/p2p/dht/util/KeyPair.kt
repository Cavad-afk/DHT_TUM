package sssemil.com.p2p.dht.util

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher


data class KeyPair(val privateKey: ByteArray, val publicKey: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as KeyPair

        if (!Arrays.equals(privateKey, other.privateKey)) return false
        if (!Arrays.equals(publicKey, other.publicKey)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Arrays.hashCode(privateKey)
        result = 31 * result + Arrays.hashCode(publicKey)
        return result
    }

    override fun toString(): String {
        return "KeyPair(privateKey=${privateKey.toHexString()}, publicKey=${publicKey.toHexString()})"
    }

    companion object {

        fun decrypt(privateKeyBytes: ByteArray, data: ByteArray): ByteArray {
            val cipher = Cipher.getInstance("RSA")
            val privateKey = KeyPair.getPrivate(privateKeyBytes)

            cipher.init(Cipher.DECRYPT_MODE, privateKey)

            return cipher.doFinal(data)
        }

        fun encrypt(publicKeyBytes: ByteArray, data: ByteArray): ByteArray {
            val cipher = Cipher.getInstance("RSA")
            val publicKey = KeyPair.getPublic(publicKeyBytes)

            cipher.init(Cipher.ENCRYPT_MODE, publicKey)

            return cipher.doFinal(data)
        }

        fun getPrivate(key: ByteArray): PrivateKey {
            val spec = PKCS8EncodedKeySpec(key)
            val kf = KeyFactory.getInstance("RSA")
            return kf.generatePrivate(spec)
        }

        fun getPublic(key: ByteArray): PublicKey {
            val spec = X509EncodedKeySpec(key)
            val kf = KeyFactory.getInstance("RSA")
            return kf.generatePublic(spec)
        }
    }
}