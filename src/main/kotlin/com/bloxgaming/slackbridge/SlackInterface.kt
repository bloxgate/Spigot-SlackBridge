package com.bloxgaming.slackbridge

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

class SlackInterface(private val channel: String, private val token: String) {

    companion object {
        private val json: Gson = GsonBuilder().create()
    }

    fun sendToSlack(message: String): Boolean {
        val postContent = mapOf("channel" to channel, "message" to message)

        val client = HttpClient.newHttpClient()
        val post = HttpRequest.newBuilder(URI.create("https://slack.com/api/chat.postMessage"))
            .header("Authorization", "Bearer $token")
            .header("Content-Type", "application/json; charset=utf-8")
            .POST(HttpRequest.BodyPublishers.ofString(json.toJson(postContent)))
            .build()

        val rawResponse = client.send(post, HttpResponse.BodyHandlers.ofString())
        val postResponse = json.fromJson(rawResponse.body(), Map::class.java)

        if(!postResponse["ok"].toString().toBoolean())
        {
            val error = postResponse["error"].toString()
            SlackBridge.plugin?.logger?.warning("Error sending chat to slack: $error")

            return false
        }

        return true
    }
}