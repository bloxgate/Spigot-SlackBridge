package com.bloxgaming.slackbridge

import com.bloxgaming.slackbridge.web.InternalWebServer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class SlackInterface(private val channel: String, private val token: String) {

    companion object {
        val json: Gson = GsonBuilder().create()
        private val client = HttpClient.newHttpClient()
    }

    var server: InternalWebServer = InternalWebServer(SlackBridge.bindPort, SlackBridge.bindAddress)

    fun startSlackToMCServer() {
        server.start()
    }

    fun stopSlackToMCServer() {
        server.stop()
    }

    fun sendToSlackSynchronous(message: String): Boolean {
        val postContent = mapOf("channel" to channel, "text" to message)
        val post = HttpRequest.newBuilder(URI.create("https://slack.com/api/chat.postMessage"))
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofString(json.toJson(postContent)))
            .build()

        val rawResponse = client.send(post, HttpResponse.BodyHandlers.ofString())
        val postResponse = json.fromJson(rawResponse.body(), Map::class.java)

        if (!postResponse["ok"].toString().toBoolean()) {
            val error = postResponse["error"].toString()
            SlackBridge.plugin?.logger?.warning("Error sending chat to slack: $error")

            return false
        }

        return true
    }

    fun getSlackNameFromUserID(userID: String): String? {
        val postContent = "user=$userID"

        val post = HttpRequest.newBuilder(URI.create("https://slack.com/api/users.info"))
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofString(postContent))
            .build()

        val rawResponse = client.send(post, HttpResponse.BodyHandlers.ofString())
        val postResponse = json.fromJson(rawResponse.body(), Map::class.java)
        if (postResponse["ok"].toString().toBoolean()) {
            val user = postResponse["user"] as Map<*, *>
            val profile = user["profile"] as Map<*, *>
            return profile["real_name"].toString()
        } else {
            SlackBridge.plugin?.logger?.warning("Error getting username: ${postResponse["error"].toString()}")
        }

        return null
    }

    fun getOurSlackID(): String {
        val post = HttpRequest.newBuilder(URI.create("https://slack.com/api/auth.test"))
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.noBody())
            .build()

        val rawResponse = client.send(post, HttpResponse.BodyHandlers.ofString(Charsets.UTF_8))
        val postResponse = json.fromJson(rawResponse.body(), Map::class.java)
        if (!postResponse["ok"].toString().toBoolean()) {
            SlackBridge.plugin?.logger?.severe("Unable to get our Slack ID: ${postResponse["error"].toString()}")
            return "null"
        }

        return postResponse["user_id"].toString()
    }
}