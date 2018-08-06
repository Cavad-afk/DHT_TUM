package sssemil.com.p2p.dht

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test
import sssemil.com.p2p.dht.api.DhtObj
import sssemil.com.p2p.dht.api.model.Put
import sssemil.com.p2p.dht.util.generateKeyPair
import java.io.ByteArrayInputStream
import java.io.DataInputStream

class TestObj {

    @Test
    fun testRsa() {
        val destPair = generateKeyPair()

        val testValue = "hi".toByteArray()

        val dhtObj = DhtObj(Put(0, 0, testValue))

        val gen = dhtObj.generate(destinationPublicKey = destPair.publicKey)

        val inStr = DataInputStream(ByteArrayInputStream(gen))

        val size = inStr.readShort()

        val code = inStr.readShort()

        val parsed = DhtObj.parse(inStr, destPair)

        val parsedValue = (parsed.obj as Put).value

        assertArrayEquals(testValue, parsedValue)
    }
}