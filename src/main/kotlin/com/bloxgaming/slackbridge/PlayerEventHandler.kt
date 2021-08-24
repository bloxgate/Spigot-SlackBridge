package com.bloxgaming.slackbridge

import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerAdvancementDoneEvent
import org.bukkit.plugin.Plugin

object PlayerEventHandler : Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if (SlackBridge.deathMessages && SlackBridge.perms!!.has(event.entity, "slackbridge.chat.death")) {
            Bukkit.getScheduler().runTaskAsynchronously(SlackBridge.plugin as Plugin, Runnable {
                val message = event.deathMessage ?: "${event.entity.name} died mysteriously."
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
                val message = "${ChatColor.stripColor(event.player.displayName)} has earned the advancement [${
                    SlackBridge.advancementTitles.getOrDefault(
                        event.advancement.key.key,
                        "Unrecognized Advancement (${event.advancement.key.key})]"
                    )
                }"
                SlackBridge.slackInterface.sendToSlackSynchronous(message)
            })
        }
    }
}