package com.onous.core.command;

import com.onous.core.OnousCore;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gamemode Command
 * /gmc [player] - Creative mode
 * /gms [player] - Survival mode
 * /gma [player] - Adventure mode
 * /gmsp [player] - Spectator mode
 */
public class GamemodeCommand implements CommandExecutor, TabCompleter {

    // ═══════════════════════════════════════
    // Fields
    // ═══════════════════════════════════════
    private final OnousCore plugin;

    // ═══════════════════════════════════════
    // Constructor
    // ═══════════════════════════════════════
    
    public GamemodeCommand(OnousCore plugin) {
        this.plugin = plugin;
        
        // Register commands
        registerCommand("gmc");
        registerCommand("gms");
        registerCommand("gma");
        registerCommand("gmsp");
    }

    /**
     * Helper to register command
     */
    private void registerCommand(String name) {
        if (plugin.getCommand(name) != null) {
            plugin.getCommand(name).setExecutor(this);
            plugin.getCommand(name).setTabCompleter(this);
        }
    }

    // ═══════════════════════════════════════
    // Command Handler
    // ═══════════════════════════════════════
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();
        
        // ─────────────────────────────────────
        // Get Target Gamemode
        // ─────────────────────────────────────
        GameMode targetMode = getGameMode(cmd);
        
        if (targetMode == null) {
            return false;
        }

        // ─────────────────────────────────────
        // No Args - Self
        // ─────────────────────────────────────
        if (args.length == 0) {
            return handleSelf(sender, targetMode);
        }

        // ─────────────────────────────────────
        // With Args - Other
        // ─────────────────────────────────────
        return handleOther(sender, args[0], targetMode);
    }

    // ═══════════════════════════════════════
    // Get GameMode from Command
    // ═══════════════════════════════════════
    
    /**
     * Get GameMode from command name
     */
    private GameMode getGameMode(String cmd) {
        return switch (cmd) {
            case "gmc" -> GameMode.CREATIVE;
            case "gms" -> GameMode.SURVIVAL;
            case "gma" -> GameMode.ADVENTURE;
            case "gmsp" -> GameMode.SPECTATOR;
            default -> null;
        };
    }

    /**
     * Get display name for gamemode
     */
    private String getModeName(GameMode mode) {
        return switch (mode) {
            case CREATIVE -> "Creative";
            case SURVIVAL -> "Survival";
            case ADVENTURE -> "Adventure";
            case SPECTATOR -> "Spectator";
        };
    }

    /**
     * Get colored display name
     */
    private String getModeDisplay(GameMode mode) {
        return switch (mode) {
            case CREATIVE -> CC.ACCENT + "Creative";
            case SURVIVAL -> CC.SUCCESS + "Survival";
            case ADVENTURE -> CC.SECONDARY + "Adventure";
            case SPECTATOR -> CC.GRAY + "Spectator";
        };
    }

    // ═══════════════════════════════════════
    // Self Gamemode
    // ═══════════════════════════════════════
    
    private boolean handleSelf(CommandSender sender, GameMode mode) {
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
        if (!player.hasPermission("onous.gamemode")) {
            CC.sendPrefixed(player, plugin.getMessage("no-permission"));
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Set Gamemode
        // ─────────────────────────────────────
        setGameMode(player, mode);
        
        return true;
    }

    // ═══════════════════════════════════════
    // Other Gamemode
    // ═══════════════════════════════════════
    
    private boolean handleOther(CommandSender sender, String targetName, GameMode mode) {
        // ─────────────────────────────────────
        // Permission Check
        // ─────────────────────────────────────
        if (!sender.hasPermission("onous.gamemode.others")) {
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
        // Set Gamemode
        // ─────────────────────────────────────
        setGameMode(target, mode);

        // ─────────────────────────────────────
        // Notify Sender
        // ─────────────────────────────────────
        if (!sender.equals(target)) {
            String msg = plugin.getMessage("gamemode-changed-other")
                    .replace("{player}", target.getName())
                    .replace("{mode}", getModeName(mode));
            CC.sendPrefixed(sender, msg);
        }
        
        return true;
    }

    // ═══════════════════════════════════════
    // Set GameMode Logic
    // ═══════════════════════════════════════
    
    /**
     * Set gamemode for player
     */
    private void setGameMode(Player player, GameMode mode) {
        // Check if already in this mode
        if (player.getGameMode() == mode) {
            CC.sendPrefixed(player, CC.GRAY + "You are already in " + getModeDisplay(mode) + CC.GRAY + " mode!");
            Sounds.error(player);
            return;
        }

        // Set gamemode
        player.setGameMode(mode);

        // Notify player
        String msg = plugin.getMessage("gamemode-changed")
                .replace("{mode}", getModeName(mode));
        CC.sendPrefixed(player, msg);
        
        Sounds.success(player);
    }

    // ═══════════════════════════════════════
    // Tab Completer
    // ═══════════════════════════════════════
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // ─────────────────────────────────────
        // /gm* <player>
        // ─────────────────────────────────────
        if (args.length == 1) {
            if (!sender.hasPermission("onous.gamemode.others")) {
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