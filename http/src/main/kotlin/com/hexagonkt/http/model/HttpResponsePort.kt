package com.hexagonkt.http.model

import com.hexagonkt.http.model.ws.WsSession

interface HttpResponsePort : HttpMessage {
    val status: HttpStatus

    val contentLength: Long                        // length of response.body (or 0)

    val onConnect: WsSession.() -> Unit
    val onBinary: WsSession.(data: ByteArray) -> Unit
    val onText: WsSession.(text: String) -> Unit
    val onPing: WsSession.(data: ByteArray) -> Unit
    val onPong: WsSession.(data: ByteArray) -> Unit
    val onClose: WsSession.(status: Int, reason: String) -> Unit

    fun with(
        status: HttpStatus = this.status,
        body: Any = this.body,
        headers: Headers = this.headers,
        contentType: ContentType? = this.contentType,
        cookies: List<Cookie> = this.cookies,
        onConnect: WsSession.() -> Unit = this.onConnect,
        onBinary: WsSession.(data: ByteArray) -> Unit = this.onBinary,
        onText: WsSession.(text: String) -> Unit = this.onText,
        onPing: WsSession.(data: ByteArray) -> Unit = this.onPing,
        onPong: WsSession.(data: ByteArray) -> Unit = this.onPong,
        onClose: WsSession.(status: Int, reason: String) -> Unit = this.onClose,
    ): HttpResponsePort
}
