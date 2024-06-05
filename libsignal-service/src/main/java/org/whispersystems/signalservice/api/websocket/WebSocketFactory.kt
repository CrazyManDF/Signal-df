package org.whispersystems.signalservice.api.websocket

import org.whispersystems.signalservice.internal.websocket.WebSocketConnection

interface WebSocketFactory {
    fun createWebSocket(): WebSocketConnection
    fun createUnidentifiedWebSocket(): WebSocketConnection
}