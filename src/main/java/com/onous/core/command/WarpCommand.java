package com.onous.core.command;

import com.onous.core.OnousCore;
import com.onous.core.data.Warp;
import com.onous.core.gui.WarpGUI;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Warp Command
 * /warp [name] - Open warp GUI or teleport to warp
 * /setwarp <name> - Create a warp
 * /delwarp <name> - Delete a warp
 */
public class WarpCommand implements CommandExecutor, TabCompleter {

    // ═══════════════════════════════════════
    // Fields
    // ═══════════════════════════════════════
    private final OnousCore plugin;

    // ═══════════════════════════════════════
    // Constructor
    // ═══════════════════════════════════════
    
    public WarpCommand(OnousCore plugin) {
        this.plugin = plugin;
        
        // Register commands
        if (plugin.getCommand("warp") != null) {
            plugin.getCommand("warp").setExecutor(this);
            plugin.getCommand("warp").setTabCompleter(this);
        }
        
        if (plugin.getCommand("setwarp") != null) {
            plugin.getCommand("setwarp").setExecutor(this);
            plugin.getCommand("setwarp").setTabCompleter(this);
        }
        
        if (plugin.getCommand("delwarp") != null) {
            plugin.getCommand("delwarp").setExecutor(this);
            plugin.getCommand("delwarp").setTabCompleter(this);
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
            case "warp" -> handleWarp(player, args);
            case "setwarp" -> handleSetWarp(player, args);
            case "delwarp" -> handleDelWarp(player, args);
            default -> false;
        };
    }

    // ═══════════════════════════════════════
    // /warp [name]
    // ═══════════════════════════════════════
    
    private boolean handleWarp(Player player, String[] args) {
        // ─────────────────────────────────────
        // Permission Check
        // ─────────────────────────────────────
        if (!player.hasPermission("onous.warp")) {
            CC.sendPrefixed(player, plugin.getMessage("no-permission"));
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // No Args - Open GUI
        // ─────────────────────────────────────
        if (args.length == 0) {
            // Check if any warps exist
            if (plugin.getData().getWarps().isEmpty()) {
                CC.sendPrefixed(player, plugin.getMessage("warp-empty"));
                Sounds.error(player);
                return true;
            }
            
            new WarpGUI(plugin, player).open();
            return true;
        }

        // ─────────────────────────────────────
        // With Args - Direct Teleport
        // ─────────────────────────────────────
        String warpName = args[0];
        Warp warp = plugin.getData().getWarp(warpName);
        
        if (warp == null) {
            CC.sendPrefixed(player, plugin.getMessage("warp-not-found"));
            Sounds.error(player);
            return true;
        }

        // Check if warp is valid
        if (!warp.isValid()) {
            CC.sendPrefixed(player, plugin.getMessage("warp-not-found"));
            Sounds.error(player);
            return true;
        }

        // Teleport
        String msg = plugin.getMessage("warp-teleporting")
                .replace("{name}", warp.getDisplayName());
        CC.sendPrefixed(player, msg);
        
        plugin.getTeleport().teleport(player, warp.getLocation());
        
        return true;
    }

    // ═══════════════════════════════════════
    // /setwarp <name>
    // ═══════════════════════════════════════
    
    private boolean handleSetWarp(Player player, String[] args) {
        // ─────────────────────────────────────
        // Permission Check
        // ─────────────────────────────────────
        if (!player.hasPermission("onous.warp.set")) {
            CC.sendPrefixed(player, plugin.getMessage("no-permission"));
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Args Check
        // ─────────────────────────────────────
        if (args.length == 0) {
            CC.sendPrefixed(player, CC.ERROR + "Usage: /setwarp <name>");
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Validate Name
        // ─────────────────────────────────────
        String warpName = args[0];
        
        // Check name length
        if (warpName.length() < 2 || warpName.length() > 16) {
            CC.sendPrefixed(player, CC.ERROR + "Warp name must be 2-16 characters!");
            Sounds.error(player);
            return true;
        }
        
        // Check alphanumeric
        if (!warpName.matches("^[a-zA-Z0-9_]+$")) {
            CC.sendPrefixed(player, CC.ERROR + "Warp name can only contain letters, numbers, and underscores!");
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Check Exists
        // ─────────────────────────────────────
        if (plugin.getData().warpExists(warpName)) {
            CC.sendPrefixed(player, plugin.getMessage("warp-exists"));
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Create Warp
        // ─────────────────────────────────────
        plugin.getData().createWarp(warpName, player.getLocation());
        
        String msg = plugin.getMessage("warp-created")
                .replace("{name}", warpName);
        CC.sendPrefixed(player, msg);
        Sounds.success(player);
        
        return true;
    }

    // ═══════════════════════════════════════
    // /delwarp <name>
    // ═══════════════════════════════════════
    
    private boolean handleDelWarp(Player player, String[] args) {
        // ─────────────────────────────────────
        // Permission Check
        // ─────────────────────────────────────
        if (!player.hasPermission("onous.warp.delete")) {
            CC.sendPrefixed(player, plugin.getMessage("no-permission"));
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Args Check
        // ─────────────────────────────────────
        if (args.length == 0) {
            CC.sendPrefixed(player, CC.ERROR + "Usage: /delwarp <name>");
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Check Exists
        // ─────────────────────────────────────
        String warpName = args[0];
        
        if (!plugin.getData().warpExists(warpName)) {
            CC.sendPrefixed(player, plugin.getMessage("warp-not-found"));
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Delete Warp
        // ─────────────────────────────────────
        plugin.getData().deleteWarp(warpName);
        
        CC.sendPrefixed(player, plugin.getMessage("warp-deleted"));
        Sounds.success(player);
        
        return true;
    }

    // ═══════════════════════════════════════
    // Tab Completer
    // ═══════════════════════════════════════
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }

        String cmd = command.getName().toLowerCase();
        
        // ─────────────────────────────────────
        // /warp <name>
        // ─────────────────────────────────────
        if (cmd.equals("warp") && args.length == 1) {
            if (!sender.hasPermission("onous.warp")) {
                return new ArrayList<>();
            }
            
            String input = args[0].toLowerCase();
            return plugin.getData().getWarps().stream()
                    .map(Warp::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
        }

        // ─────────────────────────────────────
        // /delwarp <name>
        // ─────────────────────────────────────
        if (cmd.equals("delwarp") && args.length == 1) {
            if (!sender.hasPermission("onous.warp.delete")) {
                return new ArrayList<>();
            }
            
            String input = args[0].toLowerCase();
            return plugin.getData().getWarps().stream()
                    .map(Warp::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
        }

        // ─────────────────────────────────────
        // /setwarp - No suggestions
        // ─────────────────────────────────────
        
        return new ArrayList<>();
    }
}