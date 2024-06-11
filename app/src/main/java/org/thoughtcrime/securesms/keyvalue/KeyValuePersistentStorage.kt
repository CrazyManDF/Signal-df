package org.thoughtcrime.securesms.keyvalue

interface KeyValuePersistentStorage {
    fun writeDataSet(dataSet: KeyValueDataSet, removes: Collection<String>)

    val dataSet: KeyValueDataSet
}
