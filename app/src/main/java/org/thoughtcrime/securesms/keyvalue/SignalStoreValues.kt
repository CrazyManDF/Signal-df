package org.thoughtcrime.securesms.keyvalue

import org.signal.core.util.ByteSerializer
import org.signal.core.util.StringSerializer
import org.thoughtcrime.securesms.database.model.databaseprotos.SignalStoreList
import java.io.IOException
import java.util.stream.Collectors

abstract class SignalStoreValues(val store: KeyValueStore) {
    abstract fun onFirstEverAppLaunch()

    abstract fun getKeysToIncludeInBackup(): List<String>

    fun getString(key: String, defaultValue: String?): String? {
        return store.getString(key, defaultValue)
    }

    fun getInteger(key: String, defaultValue: Int): Int {
        return store.getInteger(key, defaultValue)
    }

    fun getLong(key: String, defaultValue: Long): Long {
        return store.getLong(key, defaultValue)
    }

    fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return store.getBoolean(key, defaultValue)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return store.getFloat(key, defaultValue)
    }

    fun getBlob(key: String, defaultValue: ByteArray?): ByteArray? {
        return store.getBlob(key, defaultValue)
    }

    fun <T> getObject(key: String, defaultValue: T, serializer: ByteSerializer<T>): T {
        val blob = store.getBlob(key, null)
        return if (blob == null) {
            defaultValue
        } else {
            serializer.deserialize(blob)
        }
    }

    fun <T> getList(key: String, serializer: StringSerializer<T>): List<T> {
        val blob = getBlob(key, null) ?: return emptyList()

        try {
            val signalStoreList = SignalStoreList.ADAPTER.decode(blob)

            return signalStoreList.contents.map { data: String -> serializer.deserialize(data) }
        } catch (e: IOException) {
            throw IllegalArgumentException(e)
        }
    }

    fun putBlob(key: String, value: ByteArray) {
        store.beginWrite().putBlob(key, value).apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        store.beginWrite().putBoolean(key, value).apply()
    }

    fun putFloat(key: String, value: Float) {
        store.beginWrite().putFloat(key, value).apply()
    }

    fun putInteger(key: String, value: Int) {
        store.beginWrite().putInteger(key, value).apply()
    }

    fun putLong(key: String, value: Long) {
        store.beginWrite().putLong(key, value).apply()
    }

    fun putString(key: String, value: String?) {
        store.beginWrite().putString(key, value).apply()
    }

    fun <T> putObject(key: String, value: T, serializer: ByteSerializer<T>) {
        putBlob(key, serializer.serialize(value))
    }

    fun <T> putList(key: String, values: List<T>, serializer: StringSerializer<T>) {
        putBlob(key, SignalStoreList.Builder()
            .contents(values.stream()
                .map { data: T -> serializer.serialize(data) }
                .collect(Collectors.toList()))
            .build()
            .encode())
    }

    fun remove(key: String) {
        store.beginWrite().remove(key).apply()
    }
}
