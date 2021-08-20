package com.bloxgaming.slackbridge

import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
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
}