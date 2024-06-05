package org.whispersystems.signalservice.api.websocket

interface HealthMonitor {

    fun onKeepAliveResponse(sentTimestamp: Long, isIdentifiedWebSocket: Boolean)

    fun onMessageError(status: Int, isIdentifiedWebSocket: Boolean)
}