package com.onous.core.command;

import com.onous.core.OnousCore;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TPA Command
 * /tpa <player> - Send teleport request
 * /tpaccept - Accept TPA request
 * /tpdeny - Deny TPA request
 */
public class TPACommand implements CommandExecutor, TabCompleter {

    // ═══════════════════════════════════════
    // Fields
    // ═══════════════════════════════════════
    private final OnousCore plugin;

    // ═══════════════════════════════════════
    // Constructor
    // ═══════════════════════════════════════
    
    public TPACommand(OnousCore plugin) {
        this.plugin = plugin;
        
        // Register commands
        if (plugin.getCommand("tpa") != null) {
            plugin.getCommand("tpa").setExecutor(this);
            plugin.getCommand("tpa").setTabCompleter(this);
        }
        
        if (plugin.getCommand("tpaccept") != null) {
            plugin.getCommand("tpaccept").setExecutor(this);
            plugin.getCommand("tpaccept").setTabCompleter(this);
        }
        
        if (plugin.getCommand("tpdeny") != null) {
            plugin.getCommand("tpdeny").setExecutor(this);
            plugin.getCommand("tpdeny").setTabCompleter(this);
        }
    }

    // ═══════════════════════════════════════
    // Command Handler
    // ═══════════════════════════════════════
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // ─────────────────────────────────────
        // Player Check
        // ─────────────────────────────────────
        if (!(sender instanceof Player player)) {
            CC.sendPrefixed(sender, plugin.getMessage("player-only"));
            return true;
        }

        // ─────────────────────────────────────
        // Route Command
        // ─────────────────────────────────────
        String cmd = command.getName().toLowerCase();
        
        return switch (cmd) {
            case "tpa" -> handleTPA(player, args);
            case "tpaccept" -> handleAccept(player);
            case "tpdeny" -> handleDeny(player);
            default -> false;
        };
    }

    // ═══════════════════════════════════════
    // /tpa <player>
    // ═══════════════════════════════════════
    
    private boolean handleTPA(Player player, String[] args) {
        // ─────────────────────────────────────
        // Permission Check
        // ─────────────────────────────────────
        if (!player.hasPermission("onous.tpa")) {
            CC.sendPrefixed(player, plugin.getMessage("no-permission"));
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Args Check
        // ─────────────────────────────────────
        if (args.length == 0) {
            CC.sendPrefixed(player, CC.ERROR + "Usage: /tpa <player>");
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Find Target
        // ─────────────────────────────────────
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null || !target.isOnline()) {
            CC.sendPrefixed(player, plugin.getMessage("player-not-found"));
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Self Check
        // ─────────────────────────────────────
        if (target.equals(player)) {
            CC.sendPrefixed(player, plugin.getMessage("tpa-self"));
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Send Request
        // ─────────────────────────────────────
        plugin.getTPA().sendRequest(player, target);
        
        return true;
    }

    // ═══════════════════════════════════════
    // /tpaccept
    // ═══════════════════════════════════════
    
    private boolean handleAccept(Player player) {
        // ─────────────────────────────────────
        // Permission Check
        // ─────────────────────────────────────
        if (!player.hasPermission("onous.tpa")) {
            CC.sendPrefixed(player, plugin.getMessage("no-permission"));
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Accept Latest Request
        // ─────────────────────────────────────
        plugin.getTPA().acceptLatest(player);
        
        return true;
    }

    // ═══════════════════════════════════════
    // /tpdeny
    // ═══════════════════════════════════════
    
    private boolean handleDeny(Player player) {
        // ─────────────────────────────────────
        // Permission Check
        // ─────────────────────────────────────
        if (!player.hasPermission("onous.tpa")) {
            CC.sendPrefixed(player, plugin.getMessage("no-permission"));
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Deny Latest Request
        // ─────────────────────────────────────
        plugin.getTPA().denyLatest(player);
        
        return true;
    }

    // ═══════════════════════════════════════
    // Tab Completer
    // ═══════════════════════════════════════
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        String cmd = command.getName().toLowerCase();
        
        // ─────────────────────────────────────
        // /tpa <player>
        // ─────────────────────────────────────
        if (cmd.equals("tpa") && args.length == 1) {
            if (!player.hasPermission("onous.tpa")) {
                return new ArrayList<>();
            }
            
            String input = args[0].toLowerCase();
            
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.equals(player)) // Exclude self
                    .filter(p -> plugin.getData().getSettings(p.getUniqueId()).isTpaEnabled()) // Only TPA enabled
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
        }

        // ─────────────────────────────────────
        // /tpaccept, /tpdeny - No args
        // ─────────────────────────────────────
        
        return new ArrayList<>();
    }
}