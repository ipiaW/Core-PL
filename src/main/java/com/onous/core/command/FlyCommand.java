package com.onous.core.command;

import com.onous.core.OnousCore;
import com.onous.core.data.PlayerData;
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
 * Fly Command
 * /fly - Toggle fly for self
 * /fly <player> - Toggle fly for other player
 */
public class FlyCommand implements CommandExecutor, TabCompleter {

    // ═══════════════════════════════════════
    // Fields
    // ═══════════════════════════════════════
    private final OnousCore plugin;

    // ═══════════════════════════════════════
    // Constructor
    // ═══════════════════════════════════════
    
    public FlyCommand(OnousCore plugin) {
        this.plugin = plugin;
        
        // Register command
        if (plugin.getCommand("fly") != null) {
            plugin.getCommand("fly").setExecutor(this);
            plugin.getCommand("fly").setTabCompleter(this);
        }
    }

    // ═══════════════════════════════════════
    // Command Handler
    // ═══════════════════════════════════════
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // ─────────────────────────────────────
        // No Args - Toggle Self
        // ─────────────────────────────────────
        if (args.length == 0) {
            return handleFlySelf(sender);
        }

        // ─────────────────────────────────────
        // With Args - Toggle Other
        // ─────────────────────────────────────
        return handleFlyOther(sender, args[0]);
    }

    // ═══════════════════════════════════════
    // /fly (self)
    // ═══════════════════════════════════════
    
    private boolean handleFlySelf(CommandSender sender) {
        // ─────────────────────────────────────
        // Player Check
        // ─────────────────────────────────────
        if (!(sender instanceof Player player)) {
            CC.sendPrefixed(sender, plugin.getMessage("player-only"));
            return true;
        }

        // ─────────────────────────────────────
        // Permission Check
        // ─────────────────────────────────────
        if (!player.hasPermission("onous.fly")) {
            CC.sendPrefixed(player, plugin.getMessage("no-permission"));
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Toggle Fly
        // ─────────────────────────────────────
        toggleFly(player);
        
        return true;
    }

    // ═══════════════════════════════════════
    // /fly <player>
    // ═══════════════════════════════════════
    
    private boolean handleFlyOther(CommandSender sender, String targetName) {
        // ─────────────────────────────────────
        // Permission Check
        // ─────────────────────────────────────
        if (!sender.hasPermission("onous.fly.others")) {
            CC.sendPrefixed(sender, plugin.getMessage("no-permission"));
            if (sender instanceof Player player) {
                Sounds.error(player);
            }
            return true;
        }

        // ─────────────────────────────────────
        // Find Target
        // ─────────────────────────────────────
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null || !target.isOnline()) {
            CC.sendPrefixed(sender, plugin.getMessage("player-not-found"));
            if (sender instanceof Player player) {
                Sounds.error(player);
            }
            return true;
        }

        // ─────────────────────────────────────
        // Toggle Fly for Target
        // ─────────────────────────────────────
        boolean newState = toggleFly(target);

        // ─────────────────────────────────────
        // Notify Sender
        // ─────────────────────────────────────
        if (!sender.equals(target)) {
            String msg = newState 
                    ? plugin.getMessage("fly-enabled-other")
                    : plugin.getMessage("fly-disabled-other");
            msg = msg.replace("{player}", target.getName());
            CC.sendPrefixed(sender, msg);
        }
        
        return true;
    }

    // ═══════════════════════════════════════
    // Toggle Fly Logic
    // ═══════════════════════════════════════
    
    /**
     * Toggle fly for player
     * 
     * @param player Target player
     * @return New fly state (true = enabled)
     */
    private boolean toggleFly(Player player) {
        PlayerData data = plugin.getData().getSettings(player.getUniqueId());
        
        // Toggle state
        boolean newState = !data.isFlyEnabled();
        data.setFlyEnabled(newState);

        // Apply to player
        if (newState) {
            // Enable fly
            player.setAllowFlight(true);
            player.setFlying(true);
            
            CC.sendPrefixed(player, plugin.getMessage("fly-enabled"));
            Sounds.flyOn(player);
        } else {
            // Disable fly
            player.setFlying(false);
            player.setAllowFlight(false);
            
            CC.sendPrefixed(player, plugin.getMessage("fly-disabled"));
            Sounds.flyOff(player);
        }

        return newState;
    }

    // ═══════════════════════════════════════
    // Tab Completer
    // ═══════════════════════════════════════
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // ─────────────────────────────────────
        // /fly <player>
        // ─────────────────────────────────────
        if (args.length == 1) {
            if (!sender.hasPermission("onous.fly.others")) {
                return new ArrayList<>();
            }
            
            String input = args[0].toLowerCase();
            
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}