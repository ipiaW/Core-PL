package com.onous.core.util;

import com.onous.core.OnousCore;  // ← TAMBAH IMPORT INI
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Color & Chat Utility
 * Handles hex colors and message sending
 */
public final class CC {

    // ═══════════════════════════════════════
    // Hex Pattern
    // ═══════════════════════════════════════
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");

    // ═══════════════════════════════════════
    // Theme Colors
    // ═══════════════════════════════════════
    public static final String PRIMARY = "&#FFB800";
    public static final String SECONDARY = "&#FFA500";
    public static final String ACCENT = "&#FFD700";
    public static final String SUCCESS = "&#7AFF7A";
    public static final String ERROR = "&#FF6B6B";
    public static final String GRAY = "&#AAAAAA";
    public static final String DARK = "&#555555";
    public static final String WHITE = "&#FFFFFF";

    // ═══════════════════════════════════════
    // Private Constructor
    // ═══════════════════════════════════════
    private CC() {
    }

    // ═══════════════════════════════════════
    // Translate Colors
    // ═══════════════════════════════════════
    
    public static String translate(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        Matcher matcher = HEX_PATTERN.matcher(text);
        StringBuilder buffer = new StringBuilder();

        while (matcher.find()) {
            String hex = matcher.group(1);
            matcher.appendReplacement(buffer, ChatColor.of("#" + hex).toString());
        }
        matcher.appendTail(buffer);

        return ChatColor.translateAlternateColorCodes('&', buffer.toString());
    }

    // ═══════════════════════════════════════
    // Send Messages
    // ═══════════════════════════════════════
    
    public static void send(CommandSender sender, String message) {
        if (sender != null && message != null) {
            sender.sendMessage(translate(message));
        }
    }

    public static void sendPrefixed(CommandSender sender, String message) {
        if (sender != null && message != null) {
            String prefix = OnousCore.get().getPrefix();
            send(sender, prefix + message);
        }
    }

    public static void sendMessage(CommandSender sender, String configPath) {
        if (sender != null && configPath != null) {
            String message = OnousCore.get().getMessage(configPath);
            sendPrefixed(sender, message);
        }
    }

    public static void sendMessage(CommandSender sender, String configPath, String placeholder, String value) {
        if (sender != null && configPath != null) {
            String message = OnousCore.get().getMessage(configPath);
            message = message.replace("{" + placeholder + "}", value);
            sendPrefixed(sender, message);
        }
    }

    // ═══════════════════════════════════════
    // Action Bar
    // ═══════════════════════════════════════
    
    public static void actionBar(Player player, String message) {
        if (player != null && message != null) {
            player.spigot().sendMessage(
                ChatMessageType.ACTION_BAR,
                TextComponent.fromLegacyText(translate(message))
            );
        }
    }

    // ═══════════════════════════════════════
    // Utility Methods
    // ═══════════════════════════════════════
    
    public static String strip(String text) {
        if (text == null) {
            return "";
        }
        return ChatColor.stripColor(translate(text));
    }

    public static String color(String hexColor, String text) {
        return "&#" + hexColor + text;
    }

    public static String progressBar(double current, double max, int length) {
        double percent = Math.min(current / max, 1.0);
        int filled = (int) (length * percent);
        int empty = length - filled;

        StringBuilder bar = new StringBuilder();
        bar.append(SUCCESS);
        for (int i = 0; i < filled; i++) {
            bar.append("▌");
        }
        bar.append(DARK);
        for (int i = 0; i < empty; i++) {
            bar.append("▌");
        }

        return translate(bar.toString());
    }

    public static String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            long mins = seconds / 60;
            long secs = seconds % 60;
            return secs > 0 ? mins + "m " + secs + "s" : mins + "m";
        } else {
            long hours = seconds / 3600;
            long mins = (seconds % 3600) / 60;
            return mins > 0 ? hours + "h " + mins + "m" : hours + "h";
        }
    }

    public static String formatNumber(long number) {
        return String.format("%,d", number);
    }
}
