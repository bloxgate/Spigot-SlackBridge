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
        var connectMessages: Boolean = true
        lateinit var slackInterface: SlackInterface
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
        }
        setupChat()

        plugin = this

        val config = this.config
        config.addDefault("channel_id", "yourChannelID")
        config.addDefault("bot_token", "xoxb-your-token")
        config.addDefault("send_join_and_leave", true)
        config.options().copyDefaults(true)
        this.saveDefaultConfig()

        channel = config.getString("channel_id", "yourChannelID")!!
        token = config.getString("bot_token", "xoxb-your-token")!!
        connectMessages = config.getBoolean("send_join_and_leave")

        slackInterface = SlackInterface(channel, token)
        server.pluginManager.registerEvents(ChatEventHandler, this)

        val connected = slackInterface.sendToSlack("Server connected to slack!")
        if(!connected) {
            logger.severe("Unable to connect to slack - disabling")
            server.pluginManager.disablePlugin(this)
        } else {
            slackConnected = true
        }

        logger.info("SlackBridge has started!")
    }

    override fun onDisable() {
        if(slackConnected)
        {
            slackInterface.sendToSlack("Server disconnected from slack")
        }
        logger.info("SlackBridge has shutdown!")
    }
}