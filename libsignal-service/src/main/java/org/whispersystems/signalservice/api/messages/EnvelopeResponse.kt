package org.whispersystems.signalservice.api.messages

import org.whispersystems.signalservice.internal.push.Envelope
import org.whispersystems.signalservice.internal.websocket.WebSocketRequestMessage

class EnvelopeResponse(
    val envelope: Envelope,
    val serverDeliveredTimestamp: Long,
    val websocketRequest: WebSocketRequestMessage
)