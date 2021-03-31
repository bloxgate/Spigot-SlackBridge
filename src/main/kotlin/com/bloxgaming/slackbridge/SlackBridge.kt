package com.bloxgaming.slackbridge

import net.milkbowl.vault.chat.Chat
import net.milkbowl.vault.permission.Permission
import org.bukkit.plugin.java.JavaPlugin

class SlackBridge : JavaPlugin() {

    companion object {
        var perms: Permission? = null
        var chat: Chat? = null
        var plugin: JavaPlugin? = null
        var slackConnected: Boolean = false
        lateinit var channel: String
        lateinit var token: String
        lateinit var signingSecret: String
        var connectMessages: Boolean = true
        lateinit var bindAddress: String
        var bindPort: Int = 8888
        lateinit var slackInterface: SlackInterface

        var ourSlackID: String = ""
    }

    private fun setupPermissions(): Boolean {
        val permProvider = server.servicesManager.getRegistration(Permission::class.java)
        perms = permProvider?.provider

        return perms != null
    }

    private fun setupChat(): Boolean {
        val chatProvider = server.servicesManager.getRegistration(Chat::class.java)
        chat = chatProvider?.provider

        return chat != null
    }

    override fun onEnable() {
        logger.info("SlackBridge is starting...")

        if(!setupPermissions()) {
            logger.severe("Vault not found - disabling")
            server.pluginManager.disablePlugin(this)
            return
        }
        setupChat()

        plugin = this

        val config = this.config
        config.addDefault("channel_id", "yourChannelID")
        config.addDefault("bot_token", "xoxb-your-token")
        config.addDefault("signing_secret", "yourSigningSecret")
        config.addDefault("send_join_and_leave", true)
        config.addDefault("bind_address", "0.0.0.0")
        config.addDefault("bind_port", 8888)
        config.options().copyDefaults(true)
        this.saveDefaultConfig()

        channel = config.getString("channel_id", "yourChannelID")!!
        token = config.getString("bot_token", "xoxb-your-token")!!
        signingSecret = config.getString("signing_secret", "yourSigningSecret")!!
        connectMessages = config.getBoolean("send_join_and_leave")
        bindAddress = config.getString("bind_address", "0.0.0.0")!!
        bindPort = config.getInt("bind_port", 8888)

        slackInterface = SlackInterface(channel, token)
        server.pluginManager.registerEvents(ChatEventHandler, this)

        ourSlackID = slackInterface.getOurSlackID()
        if (ourSlackID == "null") {
            logger.severe("Disabling due to missing Slack ID")
            server.pluginManager.disablePlugin(this)
            return
        }
        slackInterface.startSlackToMCServer()
        val connected = slackInterface.sendToSlackSynchronous("Server connected to slack!")
        if (!connected) {
            logger.severe("Unable to connect to slack - disabling")
            server.pluginManager.disablePlugin(this)
            return
        } else {
            slackConnected = true
        }

        logger.info("SlackBridge has started!")
    }

    override fun onDisable() {
        if (slackConnected) {
            slackInterface.sendToSlackSynchronous("Server disconnected from slack")
        }
        logger.info("Stopping internal webserver...")
        slackInterface.stopSlackToMCServer()
        logger.info("SlackBridge has shutdown!")
    }
}