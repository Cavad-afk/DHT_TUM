package sssemil.com.p2p.dht

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import sssemil.com.p2p.dht.util.generateId
import sssemil.com.p2p.dht.util.generateKey

class TestStorage {

    @Test
    fun testSaving() {
        val value = generateId()
        val key = generateKey(value)

        Storage.store(key, value, Janitor.MINUTE)

        val savedValue = Storage.get(key)

        assertEquals(value, savedValue)
    }

    @Test
    fun testSavingTime() {
        val value = generateId()
        val key = generateKey(value)
        val delay = Janitor.SECOND

        Storage.store(key, value, delay)

        Thread.sleep(delay)

        val savedValue = Storage.get(key)

        assertNull(savedValue)
    }
}