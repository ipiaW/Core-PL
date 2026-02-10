package com.onous.core.command;

import com.onous.core.OnousCore;
import com.onous.core.gui.RTPGUI;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * /rtp - Open Random Teleport GUI (DonutSMP-style)
 */
public class RTPCommand implements CommandExecutor, TabCompleter {

    private final OnousCore plugin;

    public RTPCommand(OnousCore plugin) {
        this.plugin = plugin;

        if (plugin.getCommand("rtp") != null) {
            plugin.getCommand("rtp").setExecutor(this);
            plugin.getCommand("rtp").setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            CC.sendPrefixed(sender, plugin.getMessage("player-only"));
            return true;
        }

        if (!player.hasPermission("onous.rtp")) {
            CC.sendPrefixed(player, plugin.getMessage("no-permission"));
            Sounds.error(player);
            return true;
        }

        // Buka GUI RTP
        new RTPGUI(plugin, player).open();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        // /rtp tidak punya argumen
        return new ArrayList<>();
    }
}