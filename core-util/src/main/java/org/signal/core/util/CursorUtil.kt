package org.signal.core.util

import android.database.Cursor

object CursorUtil {

    fun requireLong(cursor: Cursor, column: String): Long {
        return cursor.getLong(cursor.getColumnIndexOrThrow(column))
    }
}