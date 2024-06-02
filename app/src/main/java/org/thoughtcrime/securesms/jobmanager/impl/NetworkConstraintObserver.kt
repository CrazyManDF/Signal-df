package org.thoughtcrime.securesms.jobmanager.impl

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import org.signal.core.util.logging.Log
import org.thoughtcrime.securesms.jobmanager.ConstraintObserver

class NetworkConstraintObserver(private val application: Application) : ConstraintObserver {
    override fun register(notifier: ConstraintObserver.Notifier) {

        application.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val constraint = NetworkConstraint.Factory(application).create()
                if (constraint.isMet()) {
                    notifier.onConstraintMet(REASON)
                }
            }
        }, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        )
    }

    companion object {
        private val REASON = Log.tag(NetworkConstraintObserver::class.java)
    }
}