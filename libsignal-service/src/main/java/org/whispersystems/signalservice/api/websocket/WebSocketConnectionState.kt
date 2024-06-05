package org.whispersystems.signalservice.api.websocket

enum class WebSocketConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    DISCONNECTING,
    AUTHENTICATION_FAILED,
    FAILED;

    fun isFailure(): Boolean {
        return this == AUTHENTICATION_FAILED || this == FAILED
    }
}