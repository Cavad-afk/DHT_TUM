package sssemil.com.p2p.dht.util

import com.google.common.math.BigIntegerMath
import sssemil.com.p2p.dht.api.KEY_LENGTH
import java.math.BigInteger
import java.math.RoundingMode

class IdKeyUtils {

    companion object {

        fun distance(first: ByteArray, second: ByteArray): Int {
            val firstShort = if (first.size != KEY_LENGTH) {
                generateKey(first)
            } else first

            val secondShort = if (second.size != KEY_LENGTH) {
                generateKey(second)
            } else second

            val result = BigInteger(firstShort).xor(BigInteger(secondShort)).abs()

            return if (result == BigInteger.ZERO) return 0 else BigIntegerMath.log2(result, RoundingMode.HALF_DOWN)
        }
    }
}

