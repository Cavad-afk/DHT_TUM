package sssemil.com.p2p.dht.util

import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.delay
import java.util.*

class ActiveList<T> : List<T> {

    private val listeners = LinkedList<(T) -> (Unit)>()

    private val list = LinkedList<T>()

    override val size: Int
        get() = list.size

    fun add(element: T) {
        Logger.d("Add $element")
        list.add(element)

        synchronized(listeners) {
            listeners.forEach { it.invoke(element) }
        }
    }

    private fun check() {
        list.forEach { element ->
            synchronized(listeners) {
                listeners.forEach { it.invoke(element) }
            }
        }
    }

    fun waitFor(filter: (T) -> Boolean, maxDelay: Long) = async {
        val startTime = System.currentTimeMillis()

        var gotIt: T? = null

        val listener: (T) -> (Unit) = {
            Logger.d("Received $it")
            if (filter.invoke(it)) {
                Logger.d("Filter accepted $it")
                gotIt = it
            } else {
                Logger.d("Filter rejected $it")
            }
        }

        synchronized(listeners) {
            listeners.add(listener)
        }

        check()

        while ((System.currentTimeMillis() - startTime) < maxDelay && gotIt == null) {
            delay(10)
        }

        synchronized(listeners) {
            listeners.remove(listener)
        }

        return@async gotIt
    }

    override fun contains(element: T) = list.contains(element)

    override fun containsAll(elements: Collection<T>) = list.containsAll(elements)

    override fun get(index: Int) = list[index]

    override fun indexOf(element: T) = list.indexOf(element)

    override fun isEmpty() = list.isEmpty()

    override fun iterator() = list.iterator()

    override fun lastIndexOf(element: T) = list.lastIndexOf(element)

    override fun listIterator() = list.listIterator()

    override fun listIterator(index: Int) = list.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int) = list.subList(fromIndex, toIndex)
}