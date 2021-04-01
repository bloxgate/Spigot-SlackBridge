package com.bloxgaming.slackbridge.web

import com.bloxgaming.slackbridge.SlackBridge
import com.bloxgaming.slackbridge.SlackInterface
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer
import org.bukkit.Bukkit
import java.net.InetSocketAddress
import java.security.MessageDigest
import java.time.Clock
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.abs

class InternalWebServer(port: Int, hostname: String?) {

    val server: HttpServer

    init {
        val inetAddress = if (hostname == null) {
            InetSocketAddress(port)
        } else {
            InetSocketAddress(hostname, port)
        }

        server = HttpServer.create(inetAddress, 50)
        server.createContext("/slack", SlackEventHandler)
    }

    fun start() {
        SlackBridge.plugin?.logger?.info("Starting internal webserver")
        server.start()
    }

    fun stop() {
        server.stop(0)
    }

    private object SlackEventHandler : HttpHandler {
        @ExperimentalUnsignedTypes
        override fun handle(exchange: HttpExchange) {
            if (exchange.requestMethod.equals("post", true)) {
                val timestamp = exchange.requestHeaders.getFirst("X-Slack-Request-Timestamp") ?: "0"
                val contentType = exchange.requestHeaders.getFirst("Content-Type") ?: ""
                if (abs((Clock.systemUTC().millis() / 1000L) - timestamp.toLong()) > 60 * 5 ||
                    !contentType.equals("application/json", true)
                ) {
                    exchange.sendResponseHeaders(400, -1)
                    exchange.close()
                    return
                }
                val body = exchange.requestBody.readAllBytes().toString(Charsets.UTF_8)

                val sigBase = "v0:$timestamp:$body"
                val hmac = Mac.getInstance("HmacSHA256")
                val signingKey = SecretKeySpec(SlackBridge.signingSecret.toByteArray(Charsets.UTF_8), "HmacSHA256")
                hmac.init(signingKey)

                //https://stackoverflow.com/a/52225984
                val signature = "v0=${
                    hmac.doFinal(sigBase.toByteArray(Charsets.UTF_8)).asUByteArray().joinToString("") {
                        it.toString(16).padStart(2, '0')
                    }
                }"
                val expectedSignature = exchange.requestHeaders.getFirst("X-Slack-Signature") ?: ""
                if (!MessageDigest.isEqual(
                        signature.toByteArray(Charsets.UTF_8),
                        expectedSignature.toByteArray(Charsets.UTF_8)
                    )
                ) {
                    exchange.sendResponseHeaders(403, -1)
                    exchange.close()
                    return
                }

                val content = SlackInterface.json.fromJson(body, Map::class.java)
                if (content["challenge"] != null && content["type"].toString() == "url_verification") {
                    val response = mapOf("challenge" to content["challenge"].toString())
                    val responseJson = SlackInterface.json.toJson(response).toByteArray()

                    exchange.sendResponseHeaders(200, responseJson.size.toLong())
                    exchange.responseHeaders["Content-Type"] = "application/json; charset=utf-8"
                    exchange.responseBody.write(responseJson)
                    exchange.close()
                } else if (content["type"].toString() == "event_callback") {
                    exchange.sendResponseHeaders(200, -1)
                    exchange.close()

                    val event = content["event"] as Map<*, *>
                    if (event["type"].toString() == "message" && event["subtype"].toString() == "null") {
                        val channel = event["channel"].toString()
                        val sender = event["user"].toString()
                        val message = event["text"].toString()

                        //Check if we sent the message
                        //Also works around the slack bug described at https://api.slack.com/events/message/message_replied
                        if (sender == SlackBridge.ourSlackID || event["thread_ts"].toString() != "null") {
                            return
                        }

                        if (channel == SlackBridge.channel) {
                            val username =
                                "[Slack] <${SlackBridge.slackInterface.getSlackNameFromUserID(sender) ?: "Error"}>"
                            Bukkit.broadcastMessage("$username $message")
                        }
                    }
                }

            } else {
                exchange.sendResponseHeaders(405, -1)
                exchange.close()
            }
        }
    }
}