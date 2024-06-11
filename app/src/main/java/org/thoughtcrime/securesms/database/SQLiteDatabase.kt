package org.thoughtcrime.securesms.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteTransactionListener
import android.os.CancellationSignal
import android.util.Pair
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.sqlite.db.SupportSQLiteStatement
import net.zetetic.database.sqlcipher.SQLiteDatabase
import java.util.Locale


class SQLiteDatabase(private val wrapped: SQLiteDatabase) : SupportSQLiteDatabase {

    companion object {
        const val CONFLICT_ROLLBACK: Int = 1
        const val CONFLICT_ABORT: Int = 2
        const val CONFLICT_FAIL: Int = 3
        const val CONFLICT_IGNORE: Int = 4
        const val CONFLICT_REPLACE: Int = 5
        const val CONFLICT_NONE: Int = 0

        private const val KEY_QUERY = "query"
        private const val KEY_TABLE = "table"
        private const val KEY_THREAD = "thread"
        private const val NAME_LOCK = "LOCK"
    }

//    private val tracer = Tracer


    fun runPostSuccessfulTransaction(dedupeKey: String, task: Runnable) {
        if (wrapped.inTransaction()){

        } else {
            task.run()
        }
    }

    fun runPostSuccessfulTransaction(task: Runnable) {

    }

    fun rawQuery(sql: String, selectionArgs: Array<String>?): Cursor {
//        DatabaseMonitor.onSql(sql, selectionArgs)
        return traceSql<Cursor>("rawQuery(2a)", sql, false,
            object : Returnable<Cursor> {
                override fun run(): Cursor {
                    return wrapped.rawQuery(
                        sql,
                        selectionArgs
                    )
                }
            })
    }

    private fun <E> traceSql(
        methodName: String,
        query: String?,
        locked: Boolean,
        returnable: Returnable<E>
    ): E {
        return traceSql(methodName, null, query, locked, returnable)
    }

    private fun <E> traceSql(methodName: String , table: String? , query: String? , locked: Boolean ,returnable: Returnable<E> ): E {
        if (locked) {
//            traceLockStart()
        }
        val params: MutableMap<String, String> = HashMap()
        if (query != null) {
            params[KEY_QUERY] = query
        }
        if (table != null) {
            params[KEY_TABLE] = table
        }

//        tracer.start(methodName, params)
        val result = returnable.run()
        if (result is Cursor) {
            // Triggers filling the window (which is about to be done anyway), but lets us capture that time inside the trace
            result.count
        }
//        tracer.end(methodName)
        if (locked) {
//            traceLockEnd()
        }

        return result
    }

    private interface Returnable<E> {
        fun run(): E
    }


    override val attachedDbs: List<Pair<String, String>>?
        get() = TODO("Not yet implemented")
    override val isDatabaseIntegrityOk: Boolean
        get() = TODO("Not yet implemented")
    override val isDbLockedByCurrentThread: Boolean
        get() = TODO("Not yet implemented")
    override val isOpen: Boolean
        get() = TODO("Not yet implemented")
    override val isReadOnly: Boolean
        get() = TODO("Not yet implemented")
    override val isWriteAheadLoggingEnabled: Boolean
        get() = TODO("Not yet implemented")
    override val maximumSize: Long
        get() = TODO("Not yet implemented")
    override var pageSize: Long
        get() = TODO("Not yet implemented")
        set(value) {}
    override val path: String?
        get() = TODO("Not yet implemented")
    override var version: Int
        get() = TODO("Not yet implemented")
        set(value) {}

    override fun beginTransaction() {
        TODO("Not yet implemented")
    }

    override fun beginTransactionNonExclusive() {
        TODO("Not yet implemented")
    }

    override fun beginTransactionWithListener(transactionListener: SQLiteTransactionListener) {
        TODO("Not yet implemented")
    }

    override fun beginTransactionWithListenerNonExclusive(transactionListener: SQLiteTransactionListener) {
        TODO("Not yet implemented")
    }

    override fun close() {
        TODO("Not yet implemented")
    }

    override fun compileStatement(sql: String): SupportSQLiteStatement {
        TODO("Not yet implemented")
    }

    override fun delete(table: String, whereClause: String?, whereArgs: Array<out Any?>?): Int {
        TODO("Not yet implemented")
    }

    override fun disableWriteAheadLogging() {
        TODO("Not yet implemented")
    }

    override fun enableWriteAheadLogging(): Boolean {
        TODO("Not yet implemented")
    }

    override fun endTransaction() {
        TODO("Not yet implemented")
    }

    override fun execSQL(sql: String) {
        TODO("Not yet implemented")
    }

    override fun execSQL(sql: String, bindArgs: Array<out Any?>) {
        TODO("Not yet implemented")
    }

    override fun inTransaction(): Boolean {
        TODO("Not yet implemented")
    }

    override fun insert(table: String, conflictAlgorithm: Int, values: ContentValues): Long {
        TODO("Not yet implemented")
    }

    override fun needUpgrade(newVersion: Int): Boolean {
        TODO("Not yet implemented")
    }

    override fun query(query: SupportSQLiteQuery): Cursor {
        TODO("Not yet implemented")
    }

    override fun query(query: SupportSQLiteQuery, cancellationSignal: CancellationSignal?): Cursor {
        TODO("Not yet implemented")
    }

    override fun query(query: String): Cursor {
        TODO("Not yet implemented")
    }

    override fun query(query: String, bindArgs: Array<out Any?>): Cursor {
        TODO("Not yet implemented")
    }

    override fun setForeignKeyConstraintsEnabled(enabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setLocale(locale: Locale) {
        TODO("Not yet implemented")
    }

    override fun setMaxSqlCacheSize(cacheSize: Int) {
        TODO("Not yet implemented")
    }

    override fun setMaximumSize(numBytes: Long): Long {
        TODO("Not yet implemented")
    }

    override fun setTransactionSuccessful() {
        TODO("Not yet implemented")
    }

    override fun update(
        table: String,
        conflictAlgorithm: Int,
        values: ContentValues,
        whereClause: String?,
        whereArgs: Array<out Any?>?
    ): Int {
        TODO("Not yet implemented")
    }

    override fun yieldIfContendedSafely(): Boolean {
        TODO("Not yet implemented")
    }

    override fun yieldIfContendedSafely(sleepAfterYieldDelayMillis: Long): Boolean {
        TODO("Not yet implemented")
    }
}