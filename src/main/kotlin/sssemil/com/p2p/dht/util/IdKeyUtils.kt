package sssemil.com.p2p.dht.util

import com.google.common.math.BigIntegerMath
import java.math.BigInteger
import java.math.RoundingMode
import kotlin.experimental.xor

class IdKeyUtils {

    companion object {

        fun distance(first: ByteArray, second: ByteArray): Int {
            if (first.size != second.size) {
                throw RuntimeException("Can't work with differently sized arrays!")
            }

            val array = ByteArray(first.size)

            for (i in array.indices) {
                array[i] = first[i] xor second[i]
            }

            var bigInt = BigInteger(array)

            if (bigInt < BigInteger.ZERO) {
                bigInt = bigInt.negate()
            }

            return if (bigInt == BigInteger.ZERO) return 0 else BigIntegerMath.log2(bigInt, RoundingMode.FLOOR)
        }
    }
}

