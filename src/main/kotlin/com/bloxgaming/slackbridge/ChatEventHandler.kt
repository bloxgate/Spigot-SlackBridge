package com.bloxgaming.slackbridge

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.Plugin

object ChatEventHandler : Listener {

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        val p = event.player

        if(SlackBridge.perms?.has(p, "slackbridge.chat") == true && SlackBridge.slackConnected)
        {
            val message = event.message
            Bukkit.getScheduler().runTaskAsynchronously(SlackBridge.plugin as Plugin,
                Runnable { SlackBridge.slackInterface.sendToSlack(message) })
        }
    }
}