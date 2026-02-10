package com.onous.core.listener;

import com.onous.core.OnousCore;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

/**
 * WorldListener
 * Resolve spawn location when the spawn world gets loaded (Multiverse etc).
 */
public class WorldListener implements Listener {

    private final OnousCore plugin;

    public WorldListener(OnousCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        // ketika world baru load, coba resolve spawn jika spawn world = ini
        plugin.getData().tryResolveSpawn();
    }
}