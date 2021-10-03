package com.bloxgaming.slackbridge

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.Plugin

object ChatEventHandler : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        val p = event.player
        val displayName = ChatColor.stripColor(p.displayName)

        if (SlackBridge.perms?.has(p, "slackbridge.chat") == true && SlackBridge.slackConnected) {
            val message = "<${displayName}> ${event.message}"
            Bukkit.getScheduler().runTaskAsynchronously(SlackBridge.plugin as Plugin,
                Runnable { SlackBridge.slackInterface.sendToSlackSynchronous(message) })
        }
    }
}