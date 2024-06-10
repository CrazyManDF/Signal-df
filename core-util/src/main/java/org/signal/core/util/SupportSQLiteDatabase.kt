package org.signal.core.util

import android.database.Cursor
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteQueryBuilder

fun SupportSQLiteDatabase.select(vararg columns: String): SelectBuilderPart1 {
    return SelectBuilderPart1(this, arrayOf(*columns))
}

class SelectBuilderPart1(
    private val db: SupportSQLiteDatabase,
    private val columns: Array<String>
) {
    fun from(tableName: String): SelectBuilderPart2 {
        return SelectBuilderPart2(db, columns, tableName)
    }
}

class SelectBuilderPart2(
    private val db: SupportSQLiteDatabase,
    private val columns: Array<String>,
    private val tableName: String
) {

    fun where(where: String, vararg whereArgs: Any): SelectBuilderPart3 {
        return SelectBuilderPart3(db, columns, tableName, where, SqlUtil.buildArgs(*whereArgs))
    }

    fun where(where: String, whereArgs: Array<String>): SelectBuilderPart3 {
        return SelectBuilderPart3(db, columns, tableName, where, whereArgs)
    }

    fun run(): Cursor {
        return db.query(
            SupportSQLiteQueryBuilder
                .builder(tableName)
                .columns(columns)
                .create()
        )
    }
}

class SelectBuilderPart3(
    private val db: SupportSQLiteDatabase,
    private val columns: Array<String>,
    private val tableName: String,
    private val where: String,
    private val whereArgs: Array<String>
) {
    fun orderBy(orderBy: String): SelectBuilderPart4a {
        return SelectBuilderPart4a(db, columns, tableName, where, whereArgs, orderBy)
    }

    fun limit(limit: Int): SelectBuilderPart4b {
        return SelectBuilderPart4b(db, columns, tableName, where, whereArgs, limit.toString())
    }

    fun limit(limit: String): SelectBuilderPart4b {
        return SelectBuilderPart4b(db, columns, tableName, where, whereArgs, limit)
    }

    fun limit(limit: Int, offset: Int): SelectBuilderPart4b {
        return SelectBuilderPart4b(db, columns, tableName, where, whereArgs, "$offset,$limit")
    }

    fun run(): Cursor {
        return db.query(
            SupportSQLiteQueryBuilder
                .builder(tableName)
                .columns(columns)
                .selection(where, whereArgs)
                .create()
        )
    }
}

class SelectBuilderPart4a(
    private val db: SupportSQLiteDatabase,
    private val columns: Array<String>,
    private val tableName: String,
    private val where: String,
    private val whereArgs: Array<String>,
    private val orderBy: String
) {
    fun limit(limit: Int): SelectBuilderPart5 {
        return SelectBuilderPart5(db, columns, tableName, where, whereArgs, orderBy, limit.toString())
    }
    fun limit(limit: String): SelectBuilderPart5 {
        return SelectBuilderPart5(db, columns, tableName, where, whereArgs, orderBy, limit)
    }

    fun limit(limit: Int, offset: Int): SelectBuilderPart5 {
        return SelectBuilderPart5(db, columns, tableName, where, whereArgs, orderBy, "$offset,$limit")
    }

    fun run(): Cursor {
        return db.query(
            SupportSQLiteQueryBuilder
                .builder(tableName)
                .columns(columns)
                .selection(where, whereArgs)
                .orderBy(orderBy)
                .create()
        )
    }
}

class SelectBuilderPart4b(
    private val db: SupportSQLiteDatabase,
    private val columns: Array<String>,
    private val tableName: String,
    private val where: String,
    private val whereArgs: Array<String>,
    private val limit: String
) {
    fun orderBy(orderBy: String): SelectBuilderPart5 {
        return SelectBuilderPart5(db, columns, tableName, where, whereArgs, orderBy, limit)
    }

    fun run(): Cursor {
        return db.query(
            SupportSQLiteQueryBuilder
                .builder(tableName)
                .columns(columns)
                .selection(where, whereArgs)
                .limit(limit)
                .create()
        )
    }
}

class SelectBuilderPart5(
    private val db: SupportSQLiteDatabase,
    private val columns: Array<String>,
    private val tableName: String,
    private val where: String,
    private val whereArgs: Array<String>,
    private val orderBy: String,
    private val limit: String
) {
    fun run(): Cursor {
        return db.query(
            SupportSQLiteQueryBuilder
                .builder(tableName)
                .columns(columns)
                .selection(where, whereArgs)
                .orderBy(orderBy)
                .limit(limit)
                .create()
        )
    }
}