package sssemil.com.p2p.dht.util

import com.google.common.math.BigIntegerMath
import java.math.BigInteger
import java.math.RoundingMode

class IdKeyUtils {

    companion object {

        fun distance(first: ByteArray, second: ByteArray): Int {
            if (first.size != second.size) {
                throw RuntimeException("Can't work with differently sized arrays!")
            }

            val result = BigInteger(first).xor(BigInteger(second)).abs()

            return if (result == BigInteger.ZERO) return 0 else BigIntegerMath.log2(result, RoundingMode.HALF_DOWN)
        }
    }
}

