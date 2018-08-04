package sssemil.com.p2p.dht

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import sssemil.com.p2p.dht.util.generateKey
import sssemil.com.p2p.dht.util.generateKeyPair

class TestStorage {

    @Test
    fun testSaving() {
        val storage = Storage()

        val value = generateKeyPair().publicKey
        val key = generateKey(value)

        storage.store(key, value, Janitor.MINUTE)

        val savedValue = storage.get(key)

        assertArrayEquals(value, savedValue)
    }

    @Test
    fun testSavingTime() {
        val storage = Storage()
        val value = generateKeyPair().publicKey
        val key = generateKey(value)
        val delay = Janitor.SECOND

        storage.store(key, value, delay)

        Thread.sleep(delay + Janitor.SECOND)

        val savedValue = storage.get(key)

        assertNull(savedValue)
    }
}