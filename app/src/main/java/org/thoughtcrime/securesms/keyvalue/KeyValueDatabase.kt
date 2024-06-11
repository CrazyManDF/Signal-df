package org.thoughtcrime.securesms.keyvalue

import android.app.Application
import android.content.ContentValues
import net.zetetic.database.sqlcipher.SQLiteDatabase
import net.zetetic.database.sqlcipher.SQLiteOpenHelper
import org.signal.core.util.concurrent.SignalExecutors
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.crypto.DatabaseSecret
import org.thoughtcrime.securesms.crypto.DatabaseSecretProvider
import org.thoughtcrime.securesms.database.SignalDatabase
import org.thoughtcrime.securesms.database.SqlCipherDatabaseHook
import org.thoughtcrime.securesms.database.SqlCipherErrorHandler
import org.thoughtcrime.securesms.database.SqlCipherLibraryLoader

class KeyValueDatabase private constructor(
    private val application: Application,
    databaseSecret: DatabaseSecret
) :
    SQLiteOpenHelper(
        application,
        DATABASE_NAME,
        databaseSecret.asString(),
        null,
        DATABASE_VERSION,
        0,
        SqlCipherErrorHandler(DATABASE_NAME),
        SqlCipherDatabaseHook(),
        true
    ), KeyValuePersistentStorage {
    override fun onCreate(db: SQLiteDatabase) {
        Log.i(TAG, "onCreate()");

        db.execSQL(CREATE_TABLE)

        if (SignalDatabase.hasTable("key_value")) {
            Log.i(TAG, "Found old key_value table. Migrating data.")
//            migrateDataFromPreviousDatabase(SignalDatabase.getRawDatabase(), db)
        }
    }

    override fun onUpgrade(p0: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.i(TAG, "onUpgrade($oldVersion, $newVersion)")
    }

    override fun onOpen(db: SQLiteDatabase) {
        Log.i(TAG, "onOpen()")
        db.setForeignKeyConstraintsEnabled(true)

        SignalExecutors.BOUNDED.execute {
//            if (SignalDatabase.hasTable("key_value")) {
//                Log.i(TAG, "Dropping original key_value table from the main database.")
//                SignalDatabase.getRawDatabase().execSQL("DROP TABLE key_value")
//            }
        }
    }

    companion object {

        private val TAG = Log.tag(KeyValueDatabase::class.java)

        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "signal-key-value.db"

        private const val TABLE_NAME = "key_value"
        private const val ID = "_id"
        private const val KEY = "key"
        private const val VALUE = "value"
        private const val TYPE = "type"

        private const val CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    KEY + " TEXT UNIQUE, " +
                    VALUE + " TEXT, " +
                    TYPE + " INTEGER)"

        @Volatile
        private var instance: KeyValueDatabase? = null
        fun getInstance(context: Application): KeyValueDatabase {
            if (instance == null) {
                synchronized(KeyValueDatabase::class) {
                    if (instance == null) {
                        SqlCipherLibraryLoader.load()
                        instance = KeyValueDatabase(
                            context,
                            DatabaseSecretProvider.getOrCreateDatabaseSecret(context)
                        )
                    }
                }
            }
            return instance!!
        }
    }

    override fun writeDataSet(dataSet: KeyValueDataSet, removes: Collection<String>) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            for (entry in dataSet.values.entries) {
                val key = entry.key
                val value = entry.value
                val type = dataSet.getType(key)

                val contentValues = ContentValues(3)
                contentValues.put(KEY, key)

                when (type) {
                    ByteArray::class -> {
                        contentValues.put(VALUE, value as ByteArray)
                        contentValues.put(KEY, Type.BLOB.id)
                    }

                    Boolean::class -> {
                        contentValues.put(VALUE, value as Boolean)
                        contentValues.put(KEY, Type.BOOLEAN.id)
                    }

                    Float::class -> {
                        contentValues.put(VALUE, value as Float)
                        contentValues.put(KEY, Type.FLOAT.id)
                    }

                    Int::class -> {
                        contentValues.put(VALUE, value as Int)
                        contentValues.put(KEY, Type.INTEGER.id)
                    }

                    Long::class -> {
                        contentValues.put(VALUE, value as Long)
                        contentValues.put(KEY, Type.LONG.id)
                    }

                    String::class -> {
                        contentValues.put(VALUE, value as String)
                        contentValues.put(KEY, Type.STRING.id)
                    }

                    else -> {
                        throw AssertionError("Unknown type: $type")
                    }
                }

                db.insertWithOnConflict(
                    TABLE_NAME,
                    null,
                    contentValues,
                    SQLiteDatabase.CONFLICT_REPLACE
                )
            }

            val deleteQuery = "$KEY = ?"
            for (remove in removes) {
                db.delete(TABLE_NAME, deleteQuery, arrayOf(remove))
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    override val dataSet: KeyValueDataSet
        get() {
            val dataSet = KeyValueDataSet()
            val cursor = readableDatabase.query(TABLE_NAME)
            while (cursor != null && cursor.moveToNext()) {
                val type = Type.fromId(cursor.getInt(cursor.getColumnIndexOrThrow(TYPE)))
                val key = cursor.getString(cursor.getColumnIndexOrThrow(KEY))

                when (type) {
                    Type.BLOB -> dataSet.putBlob(
                        key,
                        cursor.getBlob(cursor.getColumnIndexOrThrow(VALUE))
                    )

                    Type.BOOLEAN -> dataSet.putBoolean(
                        key,
                        cursor.getInt(cursor.getColumnIndexOrThrow(VALUE)) == 1
                    )

                    Type.FLOAT -> dataSet.putFloat(
                        key,
                        cursor.getFloat(cursor.getColumnIndexOrThrow(VALUE))
                    )

                    Type.INTEGER -> dataSet.putInteger(
                        key,
                        cursor.getInt(cursor.getColumnIndexOrThrow(VALUE))
                    )

                    Type.LONG -> dataSet.putLong(
                        key,
                        cursor.getLong(cursor.getColumnIndexOrThrow(VALUE))
                    )

                    Type.STRING -> dataSet.putString(
                        key,
                        cursor.getString(cursor.getColumnIndexOrThrow(VALUE))
                    )
                }
            }
            return dataSet
        }

    private enum class Type(val id: Int) {
        BLOB(0), BOOLEAN(1), FLOAT(2), INTEGER(3), LONG(4), STRING(5);

        companion object {
            fun fromId(id: Int): Type {
                return entries[id]
            }
        }
    }
}