package org.thoughtcrime.securesms.database

import org.signal.core.util.logging.Log
import kotlin.concurrent.Volatile

class SignalDatabase {


    companion object {
        private val TAG = Log.tag(SignalDatabase::class.java)
        private const val DATABASE_NAME = "signal.db"

        @Volatile
        var instance: SignalDatabase? = null
            private set

        fun <T> runInTransaction(block: (SQLiteDatabase)->T): T {

        }

    }
}