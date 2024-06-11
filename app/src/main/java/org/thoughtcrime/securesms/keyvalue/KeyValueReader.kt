package org.thoughtcrime.securesms.keyvalue

interface KeyValueReader {
    fun getBlob(key: String, defaultValue: ByteArray?): ByteArray?
    fun getBoolean(key: String, defaultValue: Boolean): Boolean
    fun getFloat(key: String, defaultValue: Float): Float
    fun getInteger(key: String, defaultValue: Int): Int
    fun getLong(key: String, defaultValue: Long): Long
    fun getString(key: String, defaultValue: String?): String?
    fun containsKey(key: String): Boolean
}
