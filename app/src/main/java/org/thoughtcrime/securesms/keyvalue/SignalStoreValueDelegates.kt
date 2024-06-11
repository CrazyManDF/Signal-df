package org.thoughtcrime.securesms.keyvalue

import kotlin.reflect.KProperty

internal fun SignalStoreValues.integerValue(key: String, default: Int): SignalStoreValueDelegate<Int> {
    return IntValue(key, default, this.store)
}

internal fun SignalStoreValues.booleanValue(key: String, default: Boolean): SignalStoreValueDelegate<Boolean> {
    return BooleanValue(key, default, this.store)
}

private class BooleanValue(private val key: String, private val default: Boolean, store: KeyValueStore) : SignalStoreValueDelegate<Boolean>(store) {
    override fun getValue(values: KeyValueStore): Boolean {
        return values.getBoolean(key, default)
    }

    override fun setValue(values: KeyValueStore, value: Boolean) {
        values.beginWrite().putBoolean(key, value).apply()
    }
}

private class IntValue(private val key: String, private val default: Int, store: KeyValueStore) : SignalStoreValueDelegate<Int>(store) {
    override fun getValue(values: KeyValueStore): Int {
        return values.getInteger(key, default)
    }

    override fun setValue(values: KeyValueStore, value: Int) {
        values.beginWrite().putInteger(key, value).apply()
    }
}

sealed class SignalStoreValueDelegate<T>(private val store: KeyValueStore) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return getValue(store)
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        setValue(store, value)
    }

    internal abstract fun getValue(values: KeyValueStore): T
    internal abstract fun setValue(values: KeyValueStore, value: T)
}