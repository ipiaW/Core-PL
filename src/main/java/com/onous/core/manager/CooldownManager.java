package com.onous.core.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Cooldown Manager
 * Handles all cooldown tracking for players
 */
public class CooldownManager {

    // ═══════════════════════════════════════
    // Storage
    // ═══════════════════════════════════════
    
    /**
     * Map: PlayerUUID -> (CooldownType -> ExpireTime)
     */
    private final Map<UUID, Map<String, Long>> cooldowns;

    // ═══════════════════════════════════════
    // Constructor
    // ═══════════════════════════════════════
    
    public CooldownManager() {
        this.cooldowns = new HashMap<>();
    }

    // ═══════════════════════════════════════
    // Set Cooldown
    // ═══════════════════════════════════════
    
    /**
     * Set cooldown for player
     * 
     * @param uuid Player UUID
     * @param type Cooldown type (e.g., "tpa", "rtp", "heal")
     * @param seconds Cooldown duration in seconds
     */
    public void set(UUID uuid, String type, int seconds) {
        if (uuid == null || type == null || seconds <= 0) return;
        
        long expireTime = System.currentTimeMillis() + (seconds * 1000L);
        
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>())
                .put(type.toLowerCase(), expireTime);
    }

    /**
     * Set cooldown with milliseconds
     * 
     * @param uuid Player UUID
     * @param type Cooldown type
     * @param millis Cooldown duration in milliseconds
     */
    public void setMillis(UUID uuid, String type, long millis) {
        if (uuid == null || type == null || millis <= 0) return;
        
        long expireTime = System.currentTimeMillis() + millis;
        
        cooldowns.computeIfAbsent(uuid, k -> new HashMap<>())
                .put(type.toLowerCase(), expireTime);
    }

    // ═══════════════════════════════════════
    // Check Cooldown
    // ═══════════════════════════════════════
    
    /**
     * Check if player is on cooldown
     * 
     * @param uuid Player UUID
     * @param type Cooldown type
     * @return true if on cooldown, false if expired or not set
     */
    public boolean isOnCooldown(UUID uuid, String type) {
        if (uuid == null || type == null) return false;
        
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return false;
        
        Long expireTime = playerCooldowns.get(type.toLowerCase());
        if (expireTime == null) return false;
        
        // Check if expired
        if (System.currentTimeMillis() >= expireTime) {
            // Remove expired cooldown
            playerCooldowns.remove(type.toLowerCase());
            return false;
        }
        
        return true;
    }

    /**
     * Alias for isOnCooldown
     */
    public boolean has(UUID uuid, String type) {
        return isOnCooldown(uuid, type);
    }

    // ═══════════════════════════════════════
    // Get Remaining Time
    // ═══════════════════════════════════════
    
    /**
     * Get remaining cooldown time in seconds
     * 
     * @param uuid Player UUID
     * @param type Cooldown type
     * @return Remaining seconds, 0 if not on cooldown
     */
    public long getRemaining(UUID uuid, String type) {
        if (uuid == null || type == null) return 0;
        
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return 0;
        
        Long expireTime = playerCooldowns.get(type.toLowerCase());
        if (expireTime == null) return 0;
        
        long remaining = expireTime - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    /**
     * Get remaining cooldown time in milliseconds
     * 
     * @param uuid Player UUID
     * @param type Cooldown type
     * @return Remaining milliseconds, 0 if not on cooldown
     */
    public long getRemainingMillis(UUID uuid, String type) {
        if (uuid == null || type == null) return 0;
        
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return 0;
        
        Long expireTime = playerCooldowns.get(type.toLowerCase());
        if (expireTime == null) return 0;
        
        return Math.max(0, expireTime - System.currentTimeMillis());
    }

    // ═══════════════════════════════════════
    // Remove Cooldown
    // ═══════════════════════════════════════
    
    /**
     * Remove specific cooldown
     * 
     * @param uuid Player UUID
     * @param type Cooldown type
     */
    public void remove(UUID uuid, String type) {
        if (uuid == null || type == null) return;
        
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns != null) {
            playerCooldowns.remove(type.toLowerCase());
        }
    }

    /**
     * Remove all cooldowns for player
     * 
     * @param uuid Player UUID
     */
    public void removeAll(UUID uuid) {
        if (uuid != null) {
            cooldowns.remove(uuid);
        }
    }

    /**
     * Clear all cooldowns
     */
    public void clear() {
        cooldowns.clear();
    }

    // ═══════════════════════════════════════
    // Utility Methods
    // ═══════════════════════════════════════
    
    /**
     * Check and get remaining time (combined check)
     * Returns -1 if not on cooldown
     * 
     * @param uuid Player UUID
     * @param type Cooldown type
     * @return Remaining seconds or -1 if not on cooldown
     */
    public long checkAndGet(UUID uuid, String type) {
        if (!isOnCooldown(uuid, type)) {
            return -1;
        }
        return getRemaining(uuid, type);
    }

    /**
     * Get formatted remaining time
     * 
     * @param uuid Player UUID
     * @param type Cooldown type
     * @return Formatted time string (e.g., "2m 30s")
     */
    public String getFormattedRemaining(UUID uuid, String type) {
        long seconds = getRemaining(uuid, type);
        
        if (seconds <= 0) {
            return "0s";
        } else if (seconds < 60) {
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

    /**
     * Cleanup expired cooldowns
     * Call periodically to free memory
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        
        cooldowns.entrySet().removeIf(entry -> {
            entry.getValue().entrySet().removeIf(cd -> cd.getValue() <= now);
            return entry.getValue().isEmpty();
        });
    }

    /**
     * Get all active cooldowns for player
     * 
     * @param uuid Player UUID
     * @return Map of cooldown types to remaining seconds
     */
    public Map<String, Long> getAll(UUID uuid) {
        Map<String, Long> result = new HashMap<>();
        
        if (uuid == null) return result;
        
        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (playerCooldowns == null) return result;
        
        long now = System.currentTimeMillis();
        
        for (Map.Entry<String, Long> entry : playerCooldowns.entrySet()) {
            long remaining = (entry.getValue() - now) / 1000;
            if (remaining > 0) {
                result.put(entry.getKey(), remaining);
            }
        }
        
        return result;
    }
}
