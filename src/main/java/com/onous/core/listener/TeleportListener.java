package com.onous.core.listener;

import com.onous.core.OnousCore;
import com.onous.core.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

/**
 * Teleport Listener
 * Handles movement and damage for teleport cancellation
 * Also handles god mode
 */
public class TeleportListener implements Listener {

    // ═══════════════════════════════════════
    // Fields
    // ═══════════════════════════════════════
    private final OnousCore plugin;

    // ═══════════════════════════════════════
    // Constructor
    // ═══════════════════════════════════════
    
    public TeleportListener(OnousCore plugin) {
        this.plugin = plugin;
    }

    // ═══════════════════════════════════════
    // Player Move
    // ═══════════════════════════════════════
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        // ─────────────────────────────────────
        // Quick Check - Ignore Head Rotation
        // ─────────────────────────────────────
        
        // Only check if actually moved (not just looking around)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        // ─────────────────────────────────────
        // Check Pending Teleport
        // ─────────────────────────────────────
        
        Player player = event.getPlayer();
        
        // Notify teleport manager of movement
        plugin.getTeleport().onMove(player);
    }

    // ═══════════════════════════════════════
    // Entity Damage
    // ═══════════════════════════════════════
    
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        // ─────────────────────────────────────
        // Check if Player
        // ─────────────────────────────────────
        
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // ─────────────────────────────────────
        // God Mode Check
        // ─────────────────────────────────────
        
        PlayerData data = plugin.getData().getSettings(player.getUniqueId());
        
        if (data.isGodEnabled() && player.hasPermission("onous.god")) {
            event.setCancelled(true);
            return;
        }

        // ─────────────────────────────────────
        // Cancel Teleport on Damage
        // ─────────────────────────────────────
        
        // Notify teleport manager of damage
        plugin.getTeleport().onDamage(player);
    }

    // ═══════════════════════════════════════
    // Player Teleport (Optional Logging)
    // ═══════════════════════════════════════
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        
        // ─────────────────────────────────────
        // Cancel Pending Teleport
        // ─────────────────────────────────────
        
        // If player teleports by other means, cancel our pending teleport
        if (plugin.getTeleport().hasPending(player.getUniqueId())) {
            // Only cancel if not caused by our teleport
            if (event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
                plugin.getTeleport().cancel(player.getUniqueId());
            }
        }
    }
}