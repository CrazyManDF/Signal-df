package org.thoughtcrime.securesms.messages

import org.thoughtcrime.securesms.groups.GroupId

class ExceptionMetadata @JvmOverloads constructor(
    val sender: String,
    val senderDevice: Int,
    val groupId: GroupId? = null
)