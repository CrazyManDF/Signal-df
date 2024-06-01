package org.thoughtcrime.securesms.util

import android.content.Context
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build

object NetworkUtil {

    fun isConnected(context: Context): Boolean {
        val connectivityManager = ServiceUtil.getConnectivityManager(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            return networkCapabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) ?: false
        } else {
            val info: NetworkInfo? = getNetworkInfo(context)
            return info != null && info.isConnected
        }
    }

    private fun getNetworkInfo(context: Context): NetworkInfo? {
        return ServiceUtil.getConnectivityManager(context).getActiveNetworkInfo()
    }
}