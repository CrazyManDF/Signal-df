package org.signal.core.util

import androidx.sqlite.db.SupportSQLiteDatabase


fun <T : SupportSQLiteDatabase, R> T.withinTransaction(block: (T) -> R): R {
    beginTransaction()
    try {
        val toReturn = block(this)
        setTransactionSuccessful()
        return toReturn
    } finally {
        endTransaction()
    }
}