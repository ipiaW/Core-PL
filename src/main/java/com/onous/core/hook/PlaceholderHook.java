package com.onous.core.hook;

import com.onous.core.OnousCore;
import com.onous.core.data.PlayerData;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * PlaceholderAPI Hook
 * Provides placeholders for OnousCore
 */
public class PlaceholderHook extends PlaceholderExpansion {

    private final OnousCore plugin;

    public PlaceholderHook(OnousCore plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "onous";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
        if (player == null) return "";

        PlayerData data = plugin.getData().getSettings(player.getUniqueId());

        // ─────────────────────────────────────
        // Balance Placeholders
        // ─────────────────────────────────────
        
        // %onous_balance% - Short format (10.5k, 1.2M)
        if (params.equalsIgnoreCase("balance")) {
            return plugin.getData().formatBalanceShort(data.getBalance());
        }
        
        // %onous_balance_formatted% - Short with symbol (⛃ 10.5k)
        if (params.equalsIgnoreCase("balance_formatted")) {
            return plugin.getData().formatBalanceShortWithSymbol(data.getBalance());
        }
        
        // %onous_balance_raw% - Raw number (10000)
        if (params.equalsIgnoreCase("balance_raw")) {
            return String.valueOf(data.getBalance());
        }
        
        // %onous_balance_commas% - With commas (10,000)
        if (params.equalsIgnoreCase("balance_commas")) {
            return String.format("%,d", data.getBalance());
        }
        
        // %onous_balance_full% - Full with symbol (⛃ 10,000)
        if (params.equalsIgnoreCase("balance_full")) {
            return plugin.getData().formatBalance(data.getBalance());
        }

        // ─────────────────────────────────────
        // Settings Placeholders
        // ─────────────────────────────────────
        
        // %onous_tpa% - TPA enabled status
        if (params.equalsIgnoreCase("tpa")) {
            return data.isTpaEnabled() ? "Enabled" : "Disabled";
        }
        
        // %onous_msg% - MSG enabled status
        if (params.equalsIgnoreCase("msg")) {
            return data.isMsgEnabled() ? "Enabled" : "Disabled";
        }
        
        // %onous_sound% - Sound enabled status
        if (params.equalsIgnoreCase("sound")) {
            return data.isSoundEnabled() ? "Enabled" : "Disabled";
        }
        
        // %onous_fly% - Fly enabled status
        if (params.equalsIgnoreCase("fly")) {
            return data.isFlyEnabled() ? "Enabled" : "Disabled";
        }
        
        // %onous_god% - God enabled status
        if (params.equalsIgnoreCase("god")) {
            return data.isGodEnabled() ? "Enabled" : "Disabled";
        }

        // ─────────────────────────────────────
        // Stats Placeholders
        // ─────────────────────────────────────
        
        // %onous_warps% - Total warps count
        if (params.equalsIgnoreCase("warps")) {
            return String.valueOf(plugin.getData().getWarpCount());
        }

        return null;
    }
}
