package org.signal.core.util

import android.database.sqlite.SQLiteDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import org.signal.core.util.logging.Log

object SqlUtil {

    private val TAG = Log.tag(SqlUtil::class.java)

    private const val MAX_QUERY_ARGS = 999

    fun tableExists(db: SupportSQLiteDatabase, table: String): Boolean {
        db.query("SELECT name FROM sqlite_master WHERE type=? AND name=?", arrayOf("table", table)).use {cursor ->
            return cursor != null && cursor.moveToNext()
        }
    }

    fun buildArgs(vararg objects: Any?): Array<String> {
        return objects.map {
            when (it) {
                null -> throw NullPointerException("Cannot have null arg!")
                is DatabaseId -> it.serialize()
                else -> it.toString()
            }
        }.toTypedArray()
    }
}