package org.thoughtcrime.securesms.util

import android.content.Context
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.os.Build
import androidx.annotation.RequiresApi

object NetworkUtil {

    fun isConnected(context: Context): Boolean {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            return hasInternetConnectionM(context)
//        } else {
            val info: NetworkInfo? = getNetworkInfo(context)
            return info != null && info.isConnected
//        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private fun hasInternetConnectionM(context: Context): Boolean {
        val connectivityManager = ServiceUtil.getConnectivityManager(context)
        val activityNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activityNetwork)
        return networkCapabilities != null &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    private fun getNetworkInfo(context: Context): NetworkInfo? {
        return ServiceUtil.getConnectivityManager(context).getActiveNetworkInfo()
    }
}