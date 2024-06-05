package org.whispersystems.signalservice.api.util

import org.whispersystems.signalservice.api.push.ServiceId.ACI
import org.whispersystems.signalservice.api.push.ServiceId.PNI
import org.whispersystems.signalservice.api.push.SignalServiceAddress

interface CredentialsProvider {

    fun getAci(): ACI?
    fun getPni(): PNI?
    fun getE164(): String?
    fun getDeviceId(): Int
    fun getPassword(): String?

    fun getUsername(): String {
        val sb = StringBuilder()
        sb.append(getAci().toString())
        if (getDeviceId() != SignalServiceAddress.DEFAULT_DEVICE_ID) {
            sb.append(".")
            sb.append(getDeviceId())
        }
        return sb.toString()
    }
}