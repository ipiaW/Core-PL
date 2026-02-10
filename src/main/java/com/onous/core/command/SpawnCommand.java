package com.onous.core.command;

import com.onous.core.OnousCore;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SpawnCommand implements CommandExecutor, TabCompleter {

    private final OnousCore plugin;

    public SpawnCommand(OnousCore plugin) {
        this.plugin = plugin;

        if (plugin.getCommand("spawn") != null) {
            plugin.getCommand("spawn").setExecutor(this);
            plugin.getCommand("spawn").setTabCompleter(this);
        }

        if (plugin.getCommand("setspawn") != null) {
            plugin.getCommand("setspawn").setExecutor(this);
            plugin.getCommand("setspawn").setTabCompleter(this);
        }

        // optional: /hub
        if (plugin.getCommand("hub") != null) {
            plugin.getCommand("hub").setExecutor(this);
            plugin.getCommand("hub").setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        if (!(sender instanceof Player player)) {
            CC.sendPrefixed(sender, plugin.getMessage("player-only"));
            return true;
        }

        switch (cmd) {
            case "setspawn" -> {
                if (!player.hasPermission("onous.spawn.set")) {
                    CC.sendPrefixed(player, plugin.getMessage("no-permission"));
                    Sounds.error(player);
                    return true;
                }

                plugin.getData().setSpawn(player.getLocation());
                CC.sendPrefixed(player, plugin.getMessage("spawn-set"));
                Sounds.success(player);
                return true;
            }

            case "spawn", "hub" -> {
                if (!player.hasPermission("onous.spawn")) {
                    CC.sendPrefixed(player, plugin.getMessage("no-permission"));
                    Sounds.error(player);
                    return true;
                }

                if (!plugin.getData().hasSpawn()) {
                    CC.sendPrefixed(player, plugin.getMessage("spawn-not-set"));
                    Sounds.error(player);
                    return true;
                }

                CC.sendPrefixed(player, plugin.getMessage("spawn-teleporting"));

                // HUB STYLE: instant teleport ke lokasi yang di-set
                plugin.getTeleport().teleportInstant(player, plugin.getData().getSpawn());
                return true;
            }
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return new ArrayList<>();
    }
}