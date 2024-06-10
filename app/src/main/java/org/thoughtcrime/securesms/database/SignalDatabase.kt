package org.thoughtcrime.securesms.database

import android.app.Application
import android.content.Context
import net.zetetic.database.sqlcipher.SQLiteOpenHelper
import org.signal.core.util.SqlUtil
import org.signal.core.util.logging.Log
import org.signal.core.util.withinTransaction
import org.thoughtcrime.securesms.crypto.AttachmentSecret
import org.thoughtcrime.securesms.crypto.DatabaseSecret
import org.thoughtcrime.securesms.database.helpers.SignalDatabaseMigrations
import java.io.File
import kotlin.concurrent.Volatile

class SignalDatabase(
    private val context: Application,
    databaseSecret: DatabaseSecret,
    attachmentSecret: AttachmentSecret
) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    databaseSecret.asString(),
    null,
    SignalDatabaseMigrations.DATABASE_VERSION,
    0,
    SqlCipherErrorHandler(DATABASE_NAME),
    SqlCipherDatabaseHook(),
    true
) {

    val messageTable: MessageTable = MessageTable(context, this)

    override fun onOpen(db: net.zetetic.database.sqlcipher.SQLiteDatabase) {
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: net.zetetic.database.sqlcipher.SQLiteDatabase) {
        db.execSQL(MessageTable.CREATE_TABLE)

        executeStatements(db, MessageTable.CREATE_INDEXS)
    }

    override fun onUpgrade(
        db: net.zetetic.database.sqlcipher.SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {

    }

    val rawReadableDatabase: net.zetetic.database.sqlcipher.SQLiteDatabase
        get() = super.readableDatabase

    val rawWritableDatabase: net.zetetic.database.sqlcipher.SQLiteDatabase
        get() = super.writableDatabase

    val signalReadableDatabase: SQLiteDatabase
        get() = SQLiteDatabase(super.readableDatabase)

    val signalWritableDatabase: SQLiteDatabase
        get() = SQLiteDatabase(super.writableDatabase)


    fun getSqlCipherDatabase(): net.zetetic.database.sqlcipher.SQLiteDatabase {
        return super.writableDatabase
    }

    fun markCurrent(db: SQLiteDatabase) {
        // db.version =
    }

    private fun executeStatements(
        db: net.zetetic.database.sqlcipher.SQLiteDatabase,
        statements: Array<String>
    ) {
        for (statement in statements) {
            db.execSQL(statement)
        }
    }

    companion object {
        private val TAG = Log.tag(SignalDatabase::class.java)
        private const val DATABASE_NAME = "signal.db"

        @Volatile
        var instance: SignalDatabase? = null
            private set

        fun init(
            application: Application,
            databaseSecret: DatabaseSecret,
            attachmentSecret: AttachmentSecret
        ) {
            if (instance == null) {
                synchronized(SignalDatabase::class.java) {
                    if (instance == null) {
                        instance = SignalDatabase(application, databaseSecret, attachmentSecret)
                    }
                }
            }
        }

        val rawDatabase: net.zetetic.database.sqlcipher.SQLiteDatabase
            get() = instance!!.rawWritableDatabase

        val backupDatabase: net.zetetic.database.sqlcipher.SQLiteDatabase
            get() = instance!!.rawReadableDatabase

        val inTransaction: Boolean
            get() = instance!!.rawWritableDatabase.inTransaction()

        fun runPostSuccessfulTransaction(dedupeKey: String, task: Runnable) {
            instance!!.signalWritableDatabase.runPostSuccessfulTransaction(dedupeKey, task)
        }

        fun runPostSuccessfulTransaction(task: Runnable) {
            instance!!.signalWritableDatabase.runPostSuccessfulTransaction(task)
        }

        fun databaseFileExists(context: Context): Boolean {
            return context.getDatabasePath(DATABASE_NAME).exists()
        }

        fun getDatabaseFile(context: Context): File {
            return context.getDatabasePath(DATABASE_NAME)
        }

        fun runPostBackupRestoreTasks(database: android.database.sqlite.SQLiteDatabase) {
            database.setForeignKeyConstraintsEnabled(false)
            database.beginTransaction()

            database.endTransaction()

            instance!!.rawWritableDatabase.close()
            triggerDatabaseAccess()
        }

        fun hasTable(table: String): Boolean {
            return SqlUtil.tableExists(instance!!.rawWritableDatabase, table)
        }

        fun triggerDatabaseAccess() {
            instance!!.signalWritableDatabase
        }

        fun onApplicationLevelUpgrade() {

        }

        fun <T> runInTransaction(block: (SQLiteDatabase) -> T): T {
            return instance!!.signalWritableDatabase.withinTransaction {
                block(it)
            }
        }

        val message: MessageTable
            get() = instance!!.messageTable
    }
}