package org.thoughtcrime.securesms.database

interface ThreadIdDatabaseReference {
    fun remapThread(fromId: Long, toId: Long)
}