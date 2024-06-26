package org.thoughtcrime.securesms.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.dependencies.ApplicationDependencies
import org.thoughtcrime.securesms.jobs.MessageFetchJob

class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val ACTION_BOOT = "android.intent.action.BOOT_COMPLETED"
    }
    override fun onReceive(context: Context?, intent: Intent?) {
        if (ACTION_BOOT == intent?.action){
            Log.d("BootReceiver", "==========onReceive=")
            ApplicationDependencies.jobManager.add(MessageFetchJob())
        }
    }
}