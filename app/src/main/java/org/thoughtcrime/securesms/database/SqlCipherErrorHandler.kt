package org.thoughtcrime.securesms.database

import net.zetetic.database.DatabaseErrorHandler
import net.zetetic.database.sqlcipher.SQLiteDatabase

class SqlCipherErrorHandler(private val databaseName: String) : DatabaseErrorHandler {
    override fun onCorruption(p0: SQLiteDatabase?, p1: String?) {


    }
}