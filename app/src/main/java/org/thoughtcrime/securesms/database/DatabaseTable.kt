package org.thoughtcrime.securesms.database

import android.content.Context
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies

abstract class DatabaseTable(
    protected val context: Context,
    protected var databaseHelper: SignalDatabase ) {

    protected val ID_WHERE: String = "_id = ?"
    protected val COUNT: Array<String> = arrayOf("COUNT(*)")

    val recipientIdDatabaseTables = hashSetOf<RecipientIdDatabaseReference>()
    val threadIdDatabaseTables = hashSetOf<ThreadIdDatabaseReference>()

    init {
        if (this is RecipientIdDatabaseReference){
            recipientIdDatabaseTables.add(this)
        }
        if (this is ThreadIdDatabaseReference) {
            threadIdDatabaseTables.add(this)
        }
    }

    protected fun notifyConversationListeners(threadIds: Set<Long>) {
        ApplicationDependencies.databaseObserver.notifyConversationListeners(threadIds)
    }

    protected fun notifyConversationListeners(threadId: Long) {
        ApplicationDependencies.databaseObserver.notifyConversationListeners(threadId)
    }

    fun reset(databaseHelper: SignalDatabase?) {
        this.databaseHelper = databaseHelper!!
    }

    fun getReadableDatabase(): SQLiteDatabase {
        return databaseHelper.signalReadableDatabase
    }

    fun getWritableDatabase(): SQLiteDatabase {
        return databaseHelper.signalWritableDatabase
    }
}