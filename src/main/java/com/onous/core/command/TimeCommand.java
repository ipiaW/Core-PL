package com.onous.core.command;

import com.onous.core.OnousCore;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Time Command
 * /time <day/night/noon/midnight/sunrise/sunset> - Change world time
 * /time <ticks> - Set specific time
 */
public class TimeCommand implements CommandExecutor, TabCompleter {

    // ═══════════════════════════════════════
    // Constants
    // ═══════════════════════════════════════
    private static final long TIME_DAY = 1000;
    private static final long TIME_NOON = 6000;
    private static final long TIME_SUNSET = 12000;
    private static final long TIME_NIGHT = 13000;
    private static final long TIME_MIDNIGHT = 18000;
    private static final long TIME_SUNRISE = 23000;

    // Time presets
    private static final List<String> TIME_PRESETS = Arrays.asList(
            "day", "night", "noon", "midnight", "sunrise", "sunset"
    );

    // ═══════════════════════════════════════
    // Fields
    // ═══════════════════════════════════════
    private final OnousCore plugin;

    // ═══════════════════════════════════════
    // Constructor
    // ═══════════════════════════════════════
    
    public TimeCommand(OnousCore plugin) {
        this.plugin = plugin;
        
        // Register command
        if (plugin.getCommand("time") != null) {
            plugin.getCommand("time").setExecutor(this);
            plugin.getCommand("time").setTabCompleter(this);
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
        // Permission Check
        // ─────────────────────────────────────
        if (!player.hasPermission("onous.time")) {
            CC.sendPrefixed(player, plugin.getMessage("no-permission"));
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Args Check
        // ─────────────────────────────────────
        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        // ─────────────────────────────────────
        // Parse Time
        // ─────────────────────────────────────
        String input = args[0].toLowerCase();
        Long ticks = parseTime(input);

        if (ticks == null) {
            CC.sendPrefixed(player, CC.ERROR + "Invalid time! Use: day, night, noon, midnight, sunrise, sunset");
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Set Time
        // ─────────────────────────────────────
        World world = player.getWorld();
        world.setTime(ticks);

        // ─────────────────────────────────────
        // Notify
        // ─────────────────────────────────────
        String timeName = getTimeName(input, ticks);
        String msg = plugin.getMessage("time-changed")
                .replace("{time}", timeName);
        CC.sendPrefixed(player, msg);
        
        Sounds.success(player);
        
        return true;
    }

    // ═══════════════════════════════════════
    // Parse Time
    // ═══════════════════════════════════════
    
    /**
     * Parse time input to ticks
     * 
     * @param input Time string or number
     * @return Ticks or null if invalid
     */
    private Long parseTime(String input) {
        // Check presets first
        return switch (input.toLowerCase()) {
            case "day", "morning" -> TIME_DAY;
            case "noon", "midday" -> TIME_NOON;
            case "sunset", "dusk" -> TIME_SUNSET;
            case "night" -> TIME_NIGHT;
            case "midnight" -> TIME_MIDNIGHT;
            case "sunrise", "dawn" -> TIME_SUNRISE;
            default -> {
                // Try parse as number
                try {
                    long ticks = Long.parseLong(input);
                    // Normalize to 0-24000 range
                    yield ticks % 24000;
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
        };
    }

    /**
     * Get display name for time
     */
    private String getTimeName(String input, long ticks) {
        // Check if preset name
        String presetName = switch (input.toLowerCase()) {
            case "day", "morning" -> "Day";
            case "noon", "midday" -> "Noon";
            case "sunset", "dusk" -> "Sunset";
            case "night" -> "Night";
            case "midnight" -> "Midnight";
            case "sunrise", "dawn" -> "Sunrise";
            default -> null;
        };

        if (presetName != null) {
            return getTimeIcon(input) + " " + presetName;
        }

        // Return ticks with formatted time
        return formatTicksToTime(ticks) + " (" + ticks + " ticks)";
    }

    /**
     * Get icon for time
     */
    private String getTimeIcon(String input) {
        return switch (input.toLowerCase()) {
            case "day", "morning", "noon", "midday" -> "☀";
            case "sunset", "dusk", "sunrise", "dawn" -> "🌅";
            case "night", "midnight" -> "🌙";
            default -> "⏰";
        };
    }

    /**
     * Format ticks to readable time (24h format)
     */
    private String formatTicksToTime(long ticks) {
        // Minecraft time: 0 = 6:00, 6000 = 12:00, 18000 = 0:00
        long hours = ((ticks / 1000) + 6) % 24;
        long minutes = (ticks % 1000) * 60 / 1000;
        
        return String.format("%02d:%02d", hours, minutes);
    }

    // ═══════════════════════════════════════
    // Usage
    // ═══════════════════════════════════════
    
    /**
     * Send usage message
     */
    private void sendUsage(Player player) {
        CC.send(player, "");
        CC.send(player, CC.PRIMARY + "⏰ " + CC.WHITE + "Time Command");
        CC.send(player, "");
        CC.send(player, CC.GRAY + "  /time " + CC.WHITE + "<preset>" + CC.DARK + " - Set time");
        CC.send(player, "");
        CC.send(player, CC.GRAY + "  Presets:");
        CC.send(player, CC.DARK + "    ☀ " + CC.WHITE + "day" + CC.GRAY + ", " + CC.WHITE + "noon");
        CC.send(player, CC.DARK + "    🌅 " + CC.WHITE + "sunrise" + CC.GRAY + ", " + CC.WHITE + "sunset");
        CC.send(player, CC.DARK + "    🌙 " + CC.WHITE + "night" + CC.GRAY + ", " + CC.WHITE + "midnight");
        CC.send(player, "");
        CC.send(player, CC.GRAY + "  /time " + CC.WHITE + "<ticks>" + CC.DARK + " - Set exact time");
        CC.send(player, "");
        
        Sounds.click(player);
    }

    // ═══════════════════════════════════════
    // Tab Completer
    // ═══════════════════════════════════════
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // ─────────────────────────────────────
        // /time <preset>
        // ─────────────────────────────────────
        if (args.length == 1) {
            if (!sender.hasPermission("onous.time")) {
                return new ArrayList<>();
            }
            
            String input = args[0].toLowerCase();
            
            return TIME_PRESETS.stream()
                    .filter(preset -> preset.startsWith(input))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}