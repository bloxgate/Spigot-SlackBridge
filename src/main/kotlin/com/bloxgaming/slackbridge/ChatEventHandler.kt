package com.bloxgaming.slackbridge

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

object ChatEventHandler : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val p = event.player
        if (SlackBridge.slackConnected && SlackBridge.connectMessages && SlackBridge.perms?.has(
                p,
                "slackbridge.chat.join"
            ) == true
        ) {
            val message = "${p.name} has joined the server"
            Bukkit.getScheduler().runTaskAsynchronously(SlackBridge.plugin as Plugin,
                Runnable { SlackBridge.slackInterface.sendToSlackSynchronous(message) })
        }
    }

    @EventHandler
    fun onPlayerQuit(event: PlayerQuitEvent) {
        val p = event.player
        if (SlackBridge.slackConnected && SlackBridge.connectMessages && SlackBridge.perms?.has(
                p,
                "slackbridge.chat.leave"
            ) == true
        ) {
            val message = "${p.name} has left the server"
            Bukkit.getScheduler().runTaskAsynchronously(SlackBridge.plugin as Plugin,
                Runnable { SlackBridge.slackInterface.sendToSlackSynchronous(message) })
        }
    }

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