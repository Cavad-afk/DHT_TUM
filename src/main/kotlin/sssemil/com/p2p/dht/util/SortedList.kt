package sssemil.com.p2p.dht.util

import java.util.*

class SortedList<T : Comparable<T>?>(val maxSize: Int) : List<T> {

    internal val list = LinkedList<T>()

    override val size: Int
        get() = list.size

    fun add(element: T): Boolean {
        if (list.contains(element)) return false

        list.add(element)

        Collections.sort(list)

        if (list.size > maxSize) {
            list.removeLast()
        }

        return true
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