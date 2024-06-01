package org.thoughtcrime.securesms.util

class LRUCache<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>(maxSize / 2, 0.75f, true) {

    override fun removeEldestEntry(eldest: MutableMap.MutableEntry<K, V>?): Boolean {
        return size > maxSize
    }
}