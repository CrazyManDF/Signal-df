package org.thoughtcrime.securesms.keyvalue

import org.thoughtcrime.securesms.util.Util.toIntExact
import kotlin.reflect.KClass
import kotlin.reflect.cast

class KeyValueDataSet : KeyValueReader {
    val values: HashMap<String, Any?> = hashMapOf()
    val types: HashMap<String, KClass<*>> = hashMapOf()

    fun putBlob(key: String, value: ByteArray?) {
        values[key] = value
        types[key] = ByteArray::class
    }

    fun putBoolean(key: String, value: Boolean) {
        values[key] = value
        types[key] = Boolean::class
    }

    fun putFloat(key: String, value: Float) {
        values[key] = value
        types[key] = Float::class
    }

    fun putInteger(key: String, value: Int) {
        values[key] = value
        types[key] = Int::class
    }

    fun putLong(key: String, value: Long) {
        values[key] = value
        types[key] = Long::class
    }

    fun putString(key: String, value: String?) {
        values[key] = value
        types[key] = String::class
    }

    fun putAll(other: KeyValueDataSet) {
        values.putAll(other.values)
        types.putAll(other.types)
    }

    fun removeAll(removes: Collection<String>) {
        for (remove in removes) {
            values.remove(remove)
            types.remove(remove)
        }
    }

    override fun getBlob(key: String, defaultValue: ByteArray?): ByteArray? {
        return if (containsKey(key)) {
            readValueAsType(key, ByteArray::class, true)
        } else {
            defaultValue
        }
    }

    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        return if (containsKey(key)) {
            readValueAsType(key, Boolean::class, false)!!
        } else {
            defaultValue
        }
    }

    override fun getFloat(key: String, defaultValue: Float): Float {
        return if (containsKey(key)) {
            readValueAsType(key, Float::class, false)!!
        } else {
            defaultValue
        }
    }

    override fun getInteger(key: String, defaultValue: Int): Int {
        return if (containsKey(key)) {
            readValueAsType(key, Int::class, false)!!
        } else {
            defaultValue
        }
    }

    override fun getLong(key: String, defaultValue: Long): Long {
        return if (containsKey(key)) {
            readValueAsType(key, Long::class, false)!!
        } else {
            defaultValue
        }
    }

    override fun getString(key: String, defaultValue: String?): String? {
        return if (containsKey(key)) {
            readValueAsType(key, String::class, true)
        } else {
            defaultValue
        }
    }

    override fun containsKey(key: String): Boolean {
        return values.containsKey(key)
    }

    fun getValues(): Map<String, Any?> {
        return values
    }

    fun getType(key: String): KClass<*>? {
        return types[key]
    }

    private fun <E : Any> readValueAsType(
        key: String,
        expectedType: KClass<E>,
        nullable: Boolean
    ): E? {
        val value = values[key]
        if (value == null && nullable) {
            return null
        }

        requireNotNull(value) { "Nullability mismatch!" }

        if (value::class == expectedType) {
            return expectedType.cast(value)
        }

        if (expectedType == Int::class && value is Long) {
            return expectedType.cast(toIntExact(value))
        }

        if (expectedType == Long::class && value is Int) {
            return expectedType.cast(value.toLong())
        }

        throw IllegalArgumentException("Type mismatch!")
    }
}
