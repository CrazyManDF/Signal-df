package org.whispersystems.signalservice.internal.websocket

import java.util.Locale

class WebsocketResponse(
    private val status: Int?,
    private val body: String,
    private val headers: Map<String, String>,
    private val unidentified: Boolean
) {

    constructor(status: Int?, body: String, headers: List<String>, unidentified: Boolean) : this(
        status,
        body,
        parseHeader(headers),
        unidentified
    )

    fun getStatus(): Int? {
        return status
    }

    fun getBody(): String {
        return body
    }

    fun getHeader(key: String): String? {
        return headers.get(key.lowercase(Locale.getDefault()))
    }

    fun isUnidentified(): Boolean {
        return unidentified
    }

    companion object {

        fun parseHeader(rawHeader: List<String>): Map<String, String> {
            val headers = hashMapOf<String, String>()
            for (raw in rawHeader) {
                if (raw.isEmpty().not()) {
                    val colonIndex = raw.indexOf(":")
                    if (colonIndex > 0 && colonIndex < raw.length - 1) {
                        val key = raw.substring(0, colonIndex).trim().lowercase(Locale.getDefault())
                        val value: String = raw.substring(colonIndex + 1).trim()

                        headers[key] = value
                    }
                }
            }
            return headers
        }
    }
}