package org.thoughtcrime.securesms.database

import android.app.Application

class DatabaseObserver(application: Application) {

    companion object {

        private const val KEY_CONVERSATION = "Conversation:"
    }

    fun notifyConversationListeners(threadIds: Set<Long>) {
        for (threadId in threadIds) {
            notifyConversationListeners(threadId)
        }
    }

    fun notifyConversationListeners(threadId: Long) {
        runPostSuccessfulTransaction(KEY_CONVERSATION + threadId){

        }
    }

    fun runPostSuccessfulTransaction(dedupeKey: String, runnable: Runnable) {

    }
}