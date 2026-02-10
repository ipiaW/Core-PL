package com.onous.core.manager;

import com.onous.core.OnousCore;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportManager {

    private final OnousCore plugin;
    private final Map<UUID, TeleportRequest> pendingTeleports;

    public TeleportManager(OnousCore plugin) {
        this.plugin = plugin;
        this.pendingTeleports = new HashMap<>();
    }

    public void teleport(Player player, Location destination, Runnable onComplete) {
        UUID uuid = player.getUniqueId();

        cancel(uuid);

        int delay = plugin.getConfig().getInt("teleport.delay", 3);

        if (player.hasPermission("onous.bypass.delay") || delay <= 0) {
            execute(player, destination, onComplete);
            return;
        }

        Location startLocation = player.getLocation().clone();

        CC.actionBar(player, plugin.getMessage("teleport-starting"));
        Sounds.tick(player);

        BukkitTask task = new BukkitRunnable() {
            int countdown = delay;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    pendingTeleports.remove(uuid);
                    return;
                }

                countdown--;

                if (countdown <= 0) {
                    execute(player, destination, onComplete);
                    cancel();
                    pendingTeleports.remove(uuid);
                } else {
                    CC.actionBar(player, CC.PRIMARY + "⏳ " + CC.WHITE + countdown + "s");
                    Sounds.countdown(player, countdown);
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);

        pendingTeleports.put(uuid, new TeleportRequest(task, startLocation));
    }

    public void teleport(Player player, Location destination) {
        teleport(player, destination, null);
    }

    /** ✅ Dipakai oleh /spawn (hub-style) */
    public void teleportInstant(Player player, Location destination) {
        cancel(player.getUniqueId());
        player.teleport(destination);
        CC.actionBar(player, plugin.getMessage("teleport-success"));
        Sounds.teleport(player);
    }

    private void execute(Player player, Location destination, Runnable onComplete) {
        player.teleport(destination);
        CC.actionBar(player, plugin.getMessage("teleport-success"));
        Sounds.teleport(player);

        if (onComplete != null) onComplete.run();
    }

    public void cancel(UUID uuid) {
        TeleportRequest request = pendingTeleports.remove(uuid);
        if (request != null) request.task().cancel();
    }

    public boolean hasPending(UUID uuid) {
        return pendingTeleports.containsKey(uuid);
    }

    public Location getStartLocation(UUID uuid) {
        TeleportRequest request = pendingTeleports.get(uuid);
        return request != null ? request.startLocation() : null;
    }

    public void onMove(Player player) {
        UUID uuid = player.getUniqueId();

        if (!hasPending(uuid)) return;
        if (!plugin.getConfig().getBoolean("teleport.cancel-on-move", true)) return;

        Location start = getStartLocation(uuid);
        if (start == null) return;

        if (player.getLocation().distanceSquared(start) > 0.25) {
            cancel(uuid);
            CC.actionBar(player, plugin.getMessage("teleport-cancelled"));
            Sounds.error(player);
        }
    }

    public void onDamage(Player player) {
        UUID uuid = player.getUniqueId();

        if (!hasPending(uuid)) return;
        if (!plugin.getConfig().getBoolean("teleport.cancel-on-damage", true)) return;

        cancel(uuid);
        CC.actionBar(player, plugin.getMessage("teleport-cancelled"));
        Sounds.error(player);
    }

    private record TeleportRequest(BukkitTask task, Location startLocation) {}
}