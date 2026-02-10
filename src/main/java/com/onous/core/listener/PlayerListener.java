package com.onous.core.listener;

import com.onous.core.OnousCore;
import com.onous.core.data.PlayerData;
import com.onous.core.friend.manager.FriendManager;
import com.onous.core.friend.model.FriendData;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 * Player Listener
 * - load/save data
 * - restore fly
 * - spawn logic
 * - friend join notification
 * - cleanup TPA
 */
public class PlayerListener implements Listener {

    private final OnousCore plugin;

    public PlayerListener(OnousCore plugin) {
        this.plugin = plugin;
    }

    // ═══════════════════════════════════════
    // Player Join
    // ═══════════════════════════════════════
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load PlayerData
        PlayerData data = plugin.getData().getSettings(player.getUniqueId());
        data.setLastName(player.getName());

        // Restore fly
        if (data.isFlyEnabled() && player.hasPermission("onous.fly")) {
            player.setAllowFlight(true);
            player.setFlying(true);
        }

        // First join -> teleport to spawn if enabled
        if (!player.hasPlayedBefore()) {
            if (plugin.getConfig().getBoolean("spawn.teleport-on-first-join", true)
                    && plugin.getData().hasSpawn()
                    && plugin.getData().getSpawn() != null) {

                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    if (player.isOnline()) {
                        player.teleport(plugin.getData().getSpawn());
                        Sounds.teleport(player);
                    }
                }, 5L);
            }
        }

        // Friend join notification
        FriendManager fm = plugin.getFriends();
        if (fm != null) {
            FriendData fd = fm.getData(player.getUniqueId());

            for (java.util.UUID friendUUID : fd.getFriends()) {
                Player friendOnline = Bukkit.getPlayer(friendUUID);
                if (friendOnline == null || !friendOnline.isOnline()) continue;

                PlayerData friendData = plugin.getData().getSettings(friendUUID);
                if (!friendData.isFriendJoinNotify()) continue;

                // Kirim actionbar dan sound kecil
                CC.actionBar(friendOnline,
                        CC.PRIMARY + "Your friend " + CC.WHITE + player.getName() + CC.PRIMARY + " is now online");
                Sounds.notify(friendOnline);
            }
        }
    }

    // ═══════════════════════════════════════
    // Player Quit
    // ═══════════════════════════════════════
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // Cancel pending teleport
        plugin.getTeleport().cancel(player.getUniqueId());

        // Cancel TPA requests
        plugin.getTPA().cancelAll(player.getUniqueId());

        // Save & unload player data
        plugin.getData().unloadPlayer(player.getUniqueId());

        // Friend data: opsional unload ketika quit (bisa tetap cached juga)
        // plugin.getFriends().unloadData(player.getUniqueId());
    }

    // ═══════════════════════════════════════
    // Player Respawn
    // ═══════════════════════════════════════
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if (plugin.getConfig().getBoolean("spawn.teleport-on-respawn", true)
                && plugin.getData().hasSpawn()
                && plugin.getData().getSpawn() != null) {
            event.setRespawnLocation(plugin.getData().getSpawn());
        }

        PlayerData data = plugin.getData().getSettings(player.getUniqueId());
        if (data.isFlyEnabled() && player.hasPermission("onous.fly")) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.setAllowFlight(true);
                    player.setFlying(true);
                }
            }, 2L);
        }
    }
}