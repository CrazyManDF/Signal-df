package org.whispersystems.signalservice.api

import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import org.whispersystems.signalservice.api.websocket.WebSocketConnectionState
import org.whispersystems.signalservice.api.websocket.WebSocketFactory
import org.whispersystems.signalservice.api.websocket.WebSocketUnavailableException
import org.whispersystems.signalservice.internal.websocket.WebSocketConnection

class SignalWebSocket(private val webSocketFactory: WebSocketFactory) {

    var unidentifiedWebSocket: WebSocketConnection? = null

    val webSocketState: BehaviorSubject<WebSocketConnectionState> =
        BehaviorSubject.createDefault(WebSocketConnectionState.DISCONNECTED)
    val unidentifiedWebSocketState: BehaviorSubject<WebSocketConnectionState> =
        BehaviorSubject.createDefault(WebSocketConnectionState.DISCONNECTED)

    var webSocketStateDisposable: CompositeDisposable = CompositeDisposable()
    val unidentifiedWebSocketStateDisposable: CompositeDisposable = CompositeDisposable()

    private var canConnect = false

    companion object {
        private val TAG: String = SignalWebSocket::class.java.simpleName

        private const val SERVER_DELIVERED_TIMESTAMP_HEADER = "X-Signal-Timestamp"
    }

    @Synchronized
    fun connect() {
        canConnect = true
        try {
            webSocket
//            getUnidentifiedWebSocket() todo
        } catch (e: WebSocketUnavailableException) {
            throw AssertionError(e)
        }
    }

    @Synchronized
    fun forceNewWebSockets() {

    }

    fun sendKeepAlive() {

    }

    private var webSocket: WebSocketConnection? = null
        @Throws(WebSocketUnavailableException::class)
        get() {
            if (!canConnect) {
                throw WebSocketUnavailableException()
            }

            if (field == null || field?.isDead() == true) {
                webSocketStateDisposable.dispose()

                webSocketStateDisposable = CompositeDisposable()

                field = webSocketFactory.createWebSocket().apply {
                    val state = connect()
                        .subscribeOn(Schedulers.computation())
                        .observeOn(Schedulers.computation())
                        .subscribe(webSocketState::onNext)

                    webSocketStateDisposable.add(state)
                }
            }
            return field
        }
}