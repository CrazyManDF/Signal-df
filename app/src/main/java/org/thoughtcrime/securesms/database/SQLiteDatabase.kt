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
        Database
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