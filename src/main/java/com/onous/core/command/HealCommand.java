package com.onous.core.command;

import com.onous.core.OnousCore;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Heal & Feed Command
 * /heal [player] - Heal self or player
 * /feed [player] - Feed self or player
 */
public class HealCommand implements CommandExecutor, TabCompleter {

    // ═══════════════════════════════════════
    // Fields
    // ═══════════════════════════════════════
    private final OnousCore plugin;

    // ═══════════════════════════════════════
    // Constructor
    // ═══════════════════════════════════════
    
    public HealCommand(OnousCore plugin) {
        this.plugin = plugin;
        
        if (plugin.getCommand("heal") != null) {
            plugin.getCommand("heal").setExecutor(this);
            plugin.getCommand("heal").setTabCompleter(this);
        }
        
        if (plugin.getCommand("feed") != null) {
            plugin.getCommand("feed").setExecutor(this);
            plugin.getCommand("feed").setTabCompleter(this);
        }
    }

    // ═══════════════════════════════════════
    // Command Handler
    // ═══════════════════════════════════════
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();
        boolean isHeal = cmd.equals("heal");
        
        if (args.length == 0) {
            return handleSelf(sender, isHeal);
        }

        return handleOther(sender, args[0], isHeal);
    }

    // ═══════════════════════════════════════
    // Self Heal/Feed
    // ═══════════════════════════════════════
    
    private boolean handleSelf(CommandSender sender, boolean isHeal) {
        if (!(sender instanceof Player player)) {
            CC.sendPrefixed(sender, plugin.getMessage("player-only"));
            return true;
        }

        String permission = isHeal ? "onous.heal" : "onous.feed";
        
        if (!player.hasPermission(permission)) {
            CC.sendPrefixed(player, plugin.getMessage("no-permission"));
            Sounds.error(player);
            return true;
        }

        String cooldownType = isHeal ? "heal" : "feed";
        
        if (plugin.getCooldown().isOnCooldown(player.getUniqueId(), cooldownType)) {
            if (!player.hasPermission("onous.bypass.cooldown")) {
                long remaining = plugin.getCooldown().getRemaining(player.getUniqueId(), cooldownType);
                String msg = plugin.getMessage("cooldown").replace("{time}", String.valueOf(remaining));
                CC.sendPrefixed(player, msg);
                Sounds.error(player);
                return true;
            }
        }

        if (isHeal) {
            healPlayer(player);
            CC.sendPrefixed(player, plugin.getMessage("heal-success"));
        } else {
            feedPlayer(player);
            CC.sendPrefixed(player, plugin.getMessage("feed-success"));
        }
        
        Sounds.heal(player);

        int cooldownSeconds = plugin.getConfig().getInt("cooldowns." + cooldownType, 300);
        plugin.getCooldown().set(player.getUniqueId(), cooldownType, cooldownSeconds);
        
        return true;
    }

    // ═══════════════════════════════════════
    // Other Heal/Feed
    // ═══════════════════════════════════════
    
    private boolean handleOther(CommandSender sender, String targetName, boolean isHeal) {
        String permission = isHeal ? "onous.heal.others" : "onous.feed.others";
        
        if (!sender.hasPermission(permission)) {
            CC.sendPrefixed(sender, plugin.getMessage("no-permission"));
            if (sender instanceof Player player) {
                Sounds.error(player);
            }
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        
        if (target == null || !target.isOnline()) {
            CC.sendPrefixed(sender, plugin.getMessage("player-not-found"));
            if (sender instanceof Player player) {
                Sounds.error(player);
            }
            return true;
        }

        if (isHeal) {
            healPlayer(target);
            
            CC.sendPrefixed(target, plugin.getMessage("heal-success"));
            Sounds.heal(target);
            
            if (!sender.equals(target)) {
                String msg = plugin.getMessage("heal-other").replace("{player}", target.getName());
                CC.sendPrefixed(sender, msg);
            }
        } else {
            feedPlayer(target);
            
            CC.sendPrefixed(target, plugin.getMessage("feed-success"));
            Sounds.heal(target);
            
            if (!sender.equals(target)) {
                String msg = plugin.getMessage("feed-other").replace("{player}", target.getName());
                CC.sendPrefixed(sender, msg);
            }
        }
        
        return true;
    }

    // ═══════════════════════════════════════
    // Heal Logic
    // ═══════════════════════════════════════
    
    /**
     * Heal player to full health
     */
    private void healPlayer(Player player) {
        // Get max health - FIX untuk 1.21+
        double maxHealth = player.getAttribute(Attribute.MAX_HEALTH).getValue();
        
        // Set to max health
        player.setHealth(maxHealth);
        
        // Clear fire
        player.setFireTicks(0);
        
        // Remove negative effects
        removeNegativeEffects(player);
    }

    /**
     * Remove negative potion effects
     */
    private void removeNegativeEffects(Player player) {
        // List of negative effects to remove
        PotionEffectType[] negativeEffects = {
            PotionEffectType.POISON,
            PotionEffectType.WITHER,
            PotionEffectType.BLINDNESS,
            PotionEffectType.NAUSEA,
            PotionEffectType.HUNGER,
            PotionEffectType.WEAKNESS,
            PotionEffectType.SLOWNESS,
            PotionEffectType.MINING_FATIGUE,
            PotionEffectType.LEVITATION,
            PotionEffectType.BAD_OMEN,
            PotionEffectType.DARKNESS
        };
        
        for (PotionEffectType effect : negativeEffects) {
            if (player.hasPotionEffect(effect)) {
                player.removePotionEffect(effect);
            }
        }
    }

    // ═══════════════════════════════════════
    // Feed Logic
    // ═══════════════════════════════════════
    
    /**
     * Feed player to full hunger
     */
    private void feedPlayer(Player player) {
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setExhaustion(0.0f);
    }

    // ═══════════════════════════════════════
    // Tab Completer
    // ═══════════════════════════════════════
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        String cmd = command.getName().toLowerCase();
        String permission = cmd.equals("heal") ? "onous.heal.others" : "onous.feed.others";
        
        if (args.length == 1) {
            if (!sender.hasPermission(permission)) {
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