package org.thoughtcrime.securesms.jobmanager.impl

import android.app.Application
import android.app.job.JobInfo
import android.content.Context
import org.thoughtcrime.securesms.jobmanager.Constraint
import org.thoughtcrime.securesms.util.NetworkUtil

class NetworkConstraint(private val application: Application) : Constraint {

    override fun isMet(): Boolean {
        return isMet(application)
    }

    override fun getFactoryKey(): String {
        return  KEY
    }

    override fun applyToJobInfo(jobInfoBuilder: JobInfo.Builder) {
        jobInfoBuilder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
    }

    override fun getJobSchedulerKeyPart(): String? {
        return "NETWORK"
    }
    companion object {
        const val KEY: String = "NetworkConstraint"

        fun isMet(context: Context): Boolean {
            return NetworkUtil.isConnected(context)
        }
    }
}