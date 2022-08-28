package io.github.takusan23.browserwebsocketmirroring

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.util.*
import kotlin.collections.LinkedHashSet

fun main(args: Array<String>) {
    val classloader = Thread.currentThread().contextClassLoader
    val indexHtml = classloader.getResourceAsStream("index.html")!!.readAllBytes()
    val connections = Collections.synchronizedSet<DefaultWebSocketSession?>(LinkedHashSet())

    println("ここを見ろ！ : http://localhost:8080")

    embeddedServer(Netty, port = 8080) {
        install(WebSockets)

        routing {
            // index.html を返す
            get("/") {
                this.call.respondBytes(indexHtml, ContentType.parse("text/html"))
            }

            // 画面録画をWebSocketで受け取って、接続中のブラウザへ送り返す
            webSocket("capture") {
                connections.add(this)
                for (frame in incoming) {
                    frame as? Frame.Binary ?: continue
                    connections.forEach { it.send(frame.readBytes()) }
                }
            }
        }
    }.start(wait = true)
}