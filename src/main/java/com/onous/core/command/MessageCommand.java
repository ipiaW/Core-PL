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
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Message Command
 * /msg <player> <message> - Send private message
 * /reply <message> - Reply to last message
 */
public class MessageCommand implements CommandExecutor, TabCompleter {

    // ═══════════════════════════════════════
    // Fields
    // ═══════════════════════════════════════
    private final OnousCore plugin;

    // ═══════════════════════════════════════
    // Constructor
    // ═══════════════════════════════════════
    
    public MessageCommand(OnousCore plugin) {
        this.plugin = plugin;
        
        // Register commands
        if (plugin.getCommand("msg") != null) {
            plugin.getCommand("msg").setExecutor(this);
            plugin.getCommand("msg").setTabCompleter(this);
        }
        
        if (plugin.getCommand("reply") != null) {
            plugin.getCommand("reply").setExecutor(this);
            plugin.getCommand("reply").setTabCompleter(this);
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
        if (!player.hasPermission("onous.msg")) {
            CC.sendPrefixed(player, plugin.getMessage("no-permission"));
            Sounds.error(player);
            return true;
        }

        // ─────────────────────────────────────
        // Route Command
        // ─────────────────────────────────────
        String cmd = command.getName().toLowerCase();
        
        return switch (cmd) {
            case "msg", "tell", "w", "pm" -> handleMessage(player, args);
            case "reply", "r" -> handleReply(player, args);
            default -> false;
        };
    }

    // ═══════════════════════════════════════
    // /msg <player> <message>
    // ═══════════════════════════════════════
    
    private boolean handleMessage(Player sender, String[] args) {
        // ─────────────────────────────────────
        // Args Check
        // ─────────────────────────────────────
        if (args.length < 2) {
            CC.sendPrefixed(sender, CC.ERROR + "Usage: /msg <player> <message>");
            Sounds.error(sender);
            return true;
        }

        // ─────────────────────────────────────
        // Find Target
        // ─────────────────────────────────────
        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null || !target.isOnline()) {
            CC.sendPrefixed(sender, plugin.getMessage("player-not-found"));
            Sounds.error(sender);
            return true;
        }

        // ─────────────────────────────────────
        // Self Check
        // ─────────────────────────────────────
        if (target.equals(sender)) {
            CC.sendPrefixed(sender, CC.ERROR + "You can't message yourself!");
            Sounds.error(sender);
            return true;
        }

        // ─────────────────────────────────────
        // Check Target MSG Setting
        // ─────────────────────────────────────
        PlayerData targetData = plugin.getData().getSettings(target.getUniqueId());
        
        if (!targetData.isMsgEnabled()) {
            CC.sendPrefixed(sender, plugin.getMessage("msg-disabled"));
            Sounds.error(sender);
            return true;
        }

        // ─────────────────────────────────────
        // Build Message
        // ─────────────────────────────────────
        String message = buildMessage(args, 1);

        // ─────────────────────────────────────
        // Send Message
        // ─────────────────────────────────────
        sendPrivateMessage(sender, target, message);
        
        return true;
    }

    // ═══════════════════════════════════════
    // /reply <message>
    // ═══════════════════════════════════════
    
    private boolean handleReply(Player sender, String[] args) {
        // ─────────────────────────────────────
        // Args Check
        // ─────────────────────────────────────
        if (args.length == 0) {
            CC.sendPrefixed(sender, CC.ERROR + "Usage: /reply <message>");
            Sounds.error(sender);
            return true;
        }

        // ─────────────────────────────────────
        // Get Last Message From
        // ─────────────────────────────────────
        PlayerData senderData = plugin.getData().getSettings(sender.getUniqueId());
        UUID lastMessageFrom = senderData.getLastMessageFrom();
        
        if (lastMessageFrom == null) {
            CC.sendPrefixed(sender, plugin.getMessage("msg-no-reply"));
            Sounds.error(sender);
            return true;
        }

        // ─────────────────────────────────────
        // Find Target
        // ─────────────────────────────────────
        Player target = Bukkit.getPlayer(lastMessageFrom);
        
        if (target == null || !target.isOnline()) {
            CC.sendPrefixed(sender, plugin.getMessage("player-offline"));
            Sounds.error(sender);
            return true;
        }

        // ─────────────────────────────────────
        // Check Target MSG Setting
        // ─────────────────────────────────────
        PlayerData targetData = plugin.getData().getSettings(target.getUniqueId());
        
        if (!targetData.isMsgEnabled()) {
            CC.sendPrefixed(sender, plugin.getMessage("msg-disabled"));
            Sounds.error(sender);
            return true;
        }

        // ─────────────────────────────────────
        // Build Message
        // ─────────────────────────────────────
        String message = buildMessage(args, 0);

        // ─────────────────────────────────────
        // Send Message
        // ─────────────────────────────────────
        sendPrivateMessage(sender, target, message);
        
        return true;
    }

    // ═══════════════════════════════════════
    // Send Private Message
    // ═══════════════════════════════════════
    
    /**
     * Send private message between players
     */
    private void sendPrivateMessage(Player sender, Player target, String message) {
        // ─────────────────────────────────────
        // Format Messages
        // ─────────────────────────────────────
        String sentFormat = plugin.getMessage("msg-format-sent")
                .replace("{player}", target.getName())
                .replace("{message}", message);
        
        String receivedFormat = plugin.getMessage("msg-format-received")
                .replace("{player}", sender.getName())
                .replace("{message}", message);

        // ─────────────────────────────────────
        // Send Messages
        // ─────────────────────────────────────
        CC.send(sender, sentFormat);
        CC.send(target, receivedFormat);

        // ─────────────────────────────────────
        // Play Sounds
        // ─────────────────────────────────────
        Sounds.pop(sender);
        Sounds.message(target);

        // ─────────────────────────────────────
        // Update Reply Targets
        // ─────────────────────────────────────
        PlayerData senderData = plugin.getData().getSettings(sender.getUniqueId());
        PlayerData targetData = plugin.getData().getSettings(target.getUniqueId());
        
        // Sender can reply to target
        senderData.setLastMessageFrom(target.getUniqueId());
        
        // Target can reply to sender
        targetData.setLastMessageFrom(sender.getUniqueId());
    }

    // ═══════════════════════════════════════
    // Build Message
    // ═══════════════════════════════════════
    
    /**
     * Build message from args starting at index
     */
    private String buildMessage(String[] args, int startIndex) {
        StringBuilder message = new StringBuilder();
        
        for (int i = startIndex; i < args.length; i++) {
            if (i > startIndex) {
                message.append(" ");
            }
            message.append(args[i]);
        }
        
        return message.toString();
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
        // /msg <player>
        // ─────────────────────────────────────
        if ((cmd.equals("msg") || cmd.equals("tell") || cmd.equals("w") || cmd.equals("pm")) 
                && args.length == 1) {
            
            if (!player.hasPermission("onous.msg")) {
                return new ArrayList<>();
            }
            
            String input = args[0].toLowerCase();
            
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.equals(player))  // Exclude self
                    .filter(p -> plugin.getData().getSettings(p.getUniqueId()).isMsgEnabled())  // Only msg enabled
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
        }

        // ─────────────────────────────────────
        // /reply - No suggestions
        // ─────────────────────────────────────
        
        return new ArrayList<>();
    }
}