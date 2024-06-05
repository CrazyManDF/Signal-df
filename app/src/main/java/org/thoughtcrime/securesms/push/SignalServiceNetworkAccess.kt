package org.thoughtcrime.securesms.push

import android.content.Context
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.whispersystems.signalservice.internal.configuration.SignalServiceConfiguration

open class SignalServiceNetworkAccess(context: Context) {


    fun isCensored(): Boolean {
        return isCensored(SignalStore.account().e164)
    }

    fun isCensored(number: String?): Boolean {
        return  true //getConfiguration(number) != uncensoredConfiguration
    }

    fun getConfiguration(): SignalServiceConfiguration {

    }
}