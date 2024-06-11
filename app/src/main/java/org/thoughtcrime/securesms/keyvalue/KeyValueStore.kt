package org.thoughtcrime.securesms.keyvalue

import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import org.signal.core.util.ThreadUtil
import org.signal.core.util.concurrent.SignalExecutors
import org.signal.core.util.logging.Log
import java.util.concurrent.CountDownLatch

class KeyValueStore(private val storage: KeyValuePersistentStorage) : KeyValueReader {

    private val executor = SignalExecutors.newCachedSingleThreadExecutor(
        "signal-KeyValueStore",
        ThreadUtil.PRIORITY_BACKGROUND_THREAD
    )

    private var dataSet: KeyValueDataSet? = null

    @AnyThread
    override fun getBlob(key: String, defaultValue: ByteArray?): ByteArray? {
        initializeIfNecessary()
        return dataSet!!.getBlob(key, defaultValue)
    }

    @AnyThread
    override fun getBoolean(key: String, defaultValue: Boolean): Boolean {
        initializeIfNecessary()
        return dataSet!!.getBoolean(key, defaultValue)
    }

    @AnyThread
    override fun getFloat(key: String, defaultValue: Float): Float {
        initializeIfNecessary()
        return dataSet!!.getFloat(key, defaultValue)
    }

    @AnyThread
    override fun getInteger(key: String, defaultValue: Int): Int {
        initializeIfNecessary()
        return dataSet!!.getInteger(key, defaultValue)
    }

    @AnyThread
    override fun getLong(key: String, defaultValue: Long): Long {
        initializeIfNecessary()
        return dataSet!!.getLong(key, defaultValue)
    }

    @AnyThread
    override fun getString(key: String, defaultValue: String?): String? {
        initializeIfNecessary()
        return dataSet!!.getString(key, defaultValue)
    }

    @AnyThread
    override fun containsKey(key: String): Boolean {
        initializeIfNecessary()
        return dataSet!!.containsKey(key)
    }

    @AnyThread
    fun beginWrite(): Writer {
        return Writer()
    }

    @AnyThread
    @Synchronized
    fun beginRead(): KeyValueReader {
        initializeIfNecessary()

        val copy = KeyValueDataSet()
        copy.putAll(dataSet!!)
        return copy
    }

    @AnyThread
    @Synchronized
    fun blockUntilAllWritesFinished() {
        val latch = CountDownLatch(1)

        executor.execute { latch.countDown() }

        kotlin.runCatching {
            latch.await()
        }.getOrElse {
            it.printStackTrace()
            Log.w(TAG, "Failed to wait for all writes.")
        }
    }

    @Synchronized
    fun resetCache() {
        dataSet = null
        initializeIfNecessary()
    }

    private fun write(newDataSet: KeyValueDataSet, removes: Collection<String>) {
        initializeIfNecessary()

        dataSet!!.putAll(newDataSet)
        dataSet!!.removeAll(removes)

        executor.execute { storage.writeDataSet(newDataSet, removes) }
    }
    
    private fun initializeIfNecessary() {
        if (dataSet != null) return
        this.dataSet = storage.dataSet
    }

    inner class Writer {
        private val dataSet = KeyValueDataSet()

        private val removes = hashSetOf<String>()

        fun putBlob(key: String, value: ByteArray?): Writer {
            dataSet.putBlob(key, value)
            return this
        }

        fun putBoolean(key: String, value: Boolean): Writer {
            dataSet.putBoolean(key, value)
            return this
        }

        fun putFloat(key: String, value: Float): Writer {
            dataSet.putFloat(key, value)
            return this
        }

        fun putInteger(key: String, value: Int): Writer {
            dataSet.putInteger(key, value)
            return this
        }

        fun putLong(key: String, value: Long): Writer {
            dataSet.putLong(key, value)
            return this
        }

        fun putString(key: String, value: String?): Writer {
            dataSet.putString(key, value)
            return this
        }

        fun remove(key: String): Writer {
            removes.add(key)
            return this
        }

        @AnyThread
        fun apply() {
            for (key in removes) {
                if (dataSet.containsKey(key)) {
                    throw IllegalStateException("Tried to remove a key while also setting it!")
                }
            }

            write(dataSet, removes)
        }

        @WorkerThread
        fun commit() {
            apply()
            blockUntilAllWritesFinished()
        }
    }

    companion object {
        private val TAG = Log.tag(KeyValueStore::class.java)
    }
}