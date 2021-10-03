package com.bloxgaming.slackbridge

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

object PlayerEventHandler : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val p = event.player
        if (SlackBridge.slackConnected && SlackBridge.connectMessages && SlackBridge.perms?.has(
                p,
                "slackbridge.chat.join"
            ) == true
        ) {
            val message = "<${ChatColor.stripColor(p.displayName)}> has joined the server"
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
            val message = "<${ChatColor.stripColor(p.displayName)}> has left the server"
            Bukkit.getScheduler().runTaskAsynchronously(SlackBridge.plugin as Plugin,
                Runnable { SlackBridge.slackInterface.sendToSlackSynchronous(message) })
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (SlackBridge.deathMessages && SlackBridge.perms!!.has(event.entity, "slackbridge.chat.death")) {
            Bukkit.getScheduler().runTaskAsynchronously(SlackBridge.plugin as Plugin, Runnable {
                val message =
                    event.deathMessage ?: "<${ChatColor.stripColor(event.entity.displayName)}> died mysteriously."
                SlackBridge.slackInterface.sendToSlackSynchronous(message)
            })
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerAdvancementDone(event: PlayerAdvancementDoneEvent) {
        if (event.advancement.key.key.startsWith("recipes/")) {
            return
        }
        if (SlackBridge.advancementMessages && SlackBridge.perms!!.has(event.player, "slackbridge.chat.advancement")) {
            Bukkit.getScheduler().runTaskAsynchronously(SlackBridge.plugin as Plugin, Runnable {
                val message = "<${ChatColor.stripColor(event.player.displayName)}> has earned the advancement [${
                    SlackBridge.advancementTitles.getOrDefault(
                        event.advancement.key.key,
                        "Unrecognized Advancement (${event.advancement.key.key})"
                    )
                }]"
                SlackBridge.slackInterface.sendToSlackSynchronous(message)
            })
        }
    }
}