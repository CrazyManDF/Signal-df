package org.whispersystems.signalservice.api.websocket

import java.io.IOException

class WebSocketUnavailableException : IOException("WebSocket not currently available.")