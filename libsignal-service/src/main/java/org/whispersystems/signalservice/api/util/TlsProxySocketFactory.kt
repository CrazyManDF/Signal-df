package org.whispersystems.signalservice.api.util

import okhttp3.Dns
import java.io.IOException
import java.net.InetAddress
import java.net.Socket
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.util.Optional
import javax.net.SocketFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory

class TlsProxySocketFactory(proxyHost: String, proxyPort: Int, dns: Optional<Dns>) :
    SocketFactory() {

    private val system: SSLSocketFactory

    init {
        try {
            val context = SSLContext.getInstance("TLS")
            context.init(null, null, null)
            system = context.socketFactory
        } catch (nsae: NoSuchAlgorithmException) {
            throw AssertionError(nsae)
        } catch (kme: KeyManagementException) {
            throw AssertionError(kme)
        }
    }

    override fun createSocket(host: String?, port: Int): Socket {
        TODO("Not yet implemented")
    }

    override fun createSocket(
        host: String?,
        port: Int,
        localHost: InetAddress?,
        localPort: Int
    ): Socket {
        TODO("Not yet implemented")
    }

    override fun createSocket(host: InetAddress?, port: Int): Socket {
        TODO("Not yet implemented")
    }

    override fun createSocket(
        address: InetAddress?,
        port: Int,
        localAddress: InetAddress?,
        localPort: Int
    ): Socket {
        TODO("Not yet implemented")
    }


}