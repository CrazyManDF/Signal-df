package org.thoughtcrime.securesms.messages

import org.thoughtcrime.securesms.jobmanager.JobManager

object MessageDecryptor {

    fun interface FollowUpOperation {
        fun run(): JobManager.Chain?
    }
}