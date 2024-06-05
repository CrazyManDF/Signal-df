package org.thoughtcrime.securesms.net

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

open class UserAgentInterceptor(private val userAgent: String) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(
            chain.request().newBuilder()
                .header("User-Agent", userAgent)
                .build()
        )
    }
}