package sssemil.com.p2p.dht.util

import java.security.KeyFactory
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


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

        private const val AES_KEY_LENGTH = 16

        fun decrypt(privateKeyBytes: ByteArray, data: ByteArray): ByteArray {
            val rsaCipher = Cipher.getInstance("RSA")
            val aesCipher = Cipher.getInstance("AES")

            val privateKey = KeyPair.getPrivate(privateKeyBytes)

            val aesKeyCoded = data.sliceArray(0 until 512)

            rsaCipher.init(Cipher.DECRYPT_MODE, privateKey)

            val aesKeyDecoded = SecretKeySpec(rsaCipher.doFinal(aesKeyCoded), "AES")

            aesCipher.init(Cipher.DECRYPT_MODE, aesKeyDecoded)

            val codedData = data.sliceArray(512 until data.size)

            return aesCipher.doFinal(codedData)
        }

        fun encrypt(publicKeyBytes: ByteArray, data: ByteArray): ByteArray {
            val rsaCipher = Cipher.getInstance("RSA")
            val aesCipher = Cipher.getInstance("AES")

            val aesKey = ByteArray(AES_KEY_LENGTH)
            Random().nextBytes(aesKey)

            val publicKey = KeyPair.getPublic(publicKeyBytes)

            rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey)

            val aesKeyCoded = rsaCipher.doFinal(aesKey)
            val aesKeyDecoded = SecretKeySpec(aesKey, "AES")

            aesCipher.init(Cipher.ENCRYPT_MODE, aesKeyDecoded)

            val dataCoded = aesCipher.doFinal(data)

            return aesKeyCoded + dataCoded
        }

        private fun getPrivate(key: ByteArray): PrivateKey {
            val spec = PKCS8EncodedKeySpec(key)
            val kf = KeyFactory.getInstance("RSA")
            return kf.generatePrivate(spec)
        }

        private fun getPublic(key: ByteArray): PublicKey {
            val spec = X509EncodedKeySpec(key)
            val kf = KeyFactory.getInstance("RSA")
            return kf.generatePublic(spec)
        }
    }
}