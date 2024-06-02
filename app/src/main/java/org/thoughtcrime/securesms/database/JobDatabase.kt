package org.thoughtcrime.securesms.database

import android.content.Context
import org.thoughtcrime.securesms.jobmanager.persistence.FullSpec
import org.thoughtcrime.securesms.util.SingletonHolder

open class JobDatabase private constructor(context: Context) {

    init {

    }

    fun insertJobs(fullSpecs: List<FullSpec>) {

    }

    fun markJobAsRunning(id: String, currentTime: Long) {

    }

    companion object : SingletonHolder<JobDatabase, Context>(::JobDatabase)
}
