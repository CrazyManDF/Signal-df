package org.signal.core.util

import android.database.Cursor

fun Cursor.requireLong(column: String): Long {
    return CursorUtil.requireLong(this, column)
}

fun <T> Cursor.readToSingleObject(mapper: (Cursor) -> T): T? {
    return use {
        if (it.moveToFirst()) {
            mapper(it)
        } else {
            null
        }
    }
}