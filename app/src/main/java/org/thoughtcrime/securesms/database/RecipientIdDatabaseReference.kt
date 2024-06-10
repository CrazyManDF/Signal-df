package org.thoughtcrime.securesms.database

import org.thoughtcrime.securesms.recipients.RecipientId

interface RecipientIdDatabaseReference {
    fun remapRecipient(fromId: RecipientId, toId: RecipientId)
}