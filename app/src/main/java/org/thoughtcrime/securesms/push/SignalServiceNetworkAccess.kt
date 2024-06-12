package org.thoughtcrime.securesms.push

import android.content.Context
import org.thoughtcrime.securesms.BuildConfig
import org.thoughtcrime.securesms.keyvalue.SignalStore
import org.whispersystems.signalservice.internal.configuration.SignalCdnUrl
import org.whispersystems.signalservice.internal.configuration.SignalCdsiUrl
import org.whispersystems.signalservice.internal.configuration.SignalServiceConfiguration
import org.whispersystems.signalservice.internal.configuration.SignalServiceUrl
import org.whispersystems.signalservice.internal.configuration.SignalStorageUrl
import org.whispersystems.signalservice.internal.configuration.SignalSvr2Url
import java.util.Optional

open class SignalServiceNetworkAccess(context: Context) {


    fun isCensored(): Boolean {
        return isCensored(SignalStore.account().e164)
    }

    fun isCensored(number: String?): Boolean {
        return  true //getConfiguration(number) != uncensoredConfiguration
    }

    fun getConfiguration(): SignalServiceConfiguration? {
//        return SignalServiceConfiguration()
        return null
    }

//    open val uncensoredConfiguration: SignalServiceConfiguration = SignalServiceConfiguration(
//        signalServiceUrls = arrayOf(SignalServiceUrl(BuildConfig.SIGNAL_URL, serviceTrustStore)),
//        signalCdnUrlMap = mapOf(
//            0 to arrayOf(SignalCdnUrl(BuildConfig.SIGNAL_CDN_URL, serviceTrustStore)),
//            2 to arrayOf(SignalCdnUrl(BuildConfig.SIGNAL_CDN2_URL, serviceTrustStore)),
//            3 to arrayOf(SignalCdnUrl(BuildConfig.SIGNAL_CDN3_URL, serviceTrustStore))
//        ),
//        signalStorageUrls = arrayOf(SignalStorageUrl(BuildConfig.STORAGE_URL, serviceTrustStore)),
//        signalCdsiUrls = arrayOf(SignalCdsiUrl(BuildConfig.SIGNAL_CDSI_URL, serviceTrustStore)),
//        signalSvr2Urls = arrayOf(SignalSvr2Url(BuildConfig.SIGNAL_SVR2_URL, serviceTrustStore)),
//        networkInterceptors = interceptors,
//        dns = Optional.of(DNS),
//        signalProxy = if (SignalStore.proxy().isProxyEnabled) Optional.ofNullable(SignalStore.proxy().proxy) else Optional.empty(),
//        zkGroupServerPublicParams = zkGroupServerPublicParams,
//        genericServerPublicParams = genericServerPublicParams,
//        backupServerPublicParams = backupServerPublicParams
//    )
}