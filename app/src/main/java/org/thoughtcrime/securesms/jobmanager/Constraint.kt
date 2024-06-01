package org.thoughtcrime.securesms.jobmanager

import android.app.job.JobInfo
import java.lang.StringBuilder

interface Constraint {

    fun isMet(): Boolean

    fun getFactoryKey(): String

    fun applyToJobInfo(jobInfoBuilder: JobInfo.Builder)

    fun getJobSchedulerKeyPart(): String? {
        return null
    }

    interface Factory<T : Constraint> {
        fun create(): T
    }
}