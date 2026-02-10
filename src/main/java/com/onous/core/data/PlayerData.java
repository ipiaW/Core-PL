package com.onous.core.data;

import java.util.UUID;

/**
 * Player Data Class
 * Menyimpan semua data dan setting per-player.
 */
public class PlayerData {

    // ═══════════════════════════════════════
    // Identity
    // ═══════════════════════════════════════
    private final UUID uuid;
    private String lastName;

    // ═══════════════════════════════════════
    // Settings - Toggles
    // ═══════════════════════════════════════
    private boolean tpaEnabled;
    private boolean msgEnabled;
    private boolean soundEnabled;
    private boolean randomTpaEnabled;
    private boolean privacyMode;

    // Auto accept
    private boolean autoTpa;         // Auto accept /tpa (umum)
    private boolean autoTpaHere;     // Auto accept /tpahere <player>
    private boolean autoFriendTp;    // Auto accept /tpa dari FRIEND
    private boolean friendJoinNotify;// Notif saat friend join

    // ═══════════════════════════════════════
    // Staff States
    // ═══════════════════════════════════════
    private boolean flyEnabled;
    private boolean godEnabled;

    // ═══════════════════════════════════════
    // Economy
    // ═══════════════════════════════════════
    private long balance;

    // ═══════════════════════════════════════
    // Message Reply
    // ═══════════════════════════════════════
    private UUID lastMessageFrom;

    // ═══════════════════════════════════════
    // Constructor
    // ═══════════════════════════════════════
    public PlayerData(UUID uuid) {
        this.uuid = uuid;

        // Default settings
        this.tpaEnabled = true;
        this.msgEnabled = true;
        this.soundEnabled = true;
        this.randomTpaEnabled = true;
        this.privacyMode = false;

        this.autoTpa = false;
        this.autoTpaHere = false;
        this.autoFriendTp = false;
        this.friendJoinNotify = true; // default: ON

        // Default states
        this.flyEnabled = false;
        this.godEnabled = false;

        // Balance akan di-set oleh DataManager (starting-balance)
        this.balance = 0;

        this.lastMessageFrom = null;
    }

    // ═══════════════════════════════════════
    // Identity
    // ═══════════════════════════════════════
    public UUID getUuid() {
        return uuid;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    // ═══════════════════════════════════════
    // Settings
    // ═══════════════════════════════════════
    public boolean isTpaEnabled() {
        return tpaEnabled;
    }

    public void setTpaEnabled(boolean tpaEnabled) {
        this.tpaEnabled = tpaEnabled;
    }

    public boolean isMsgEnabled() {
        return msgEnabled;
    }

    public void setMsgEnabled(boolean msgEnabled) {
        this.msgEnabled = msgEnabled;
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public void setSoundEnabled(boolean soundEnabled) {
        this.soundEnabled = soundEnabled;
    }

    public boolean isRandomTpaEnabled() {
        return randomTpaEnabled;
    }

    public void setRandomTpaEnabled(boolean randomTpaEnabled) {
        this.randomTpaEnabled = randomTpaEnabled;
    }

    public boolean isPrivacyMode() {
        return privacyMode;
    }

    public void setPrivacyMode(boolean privacyMode) {
        this.privacyMode = privacyMode;
    }

    public boolean isAutoTpa() {
        return autoTpa;
    }

    public void setAutoTpa(boolean autoTpa) {
        this.autoTpa = autoTpa;
    }

    public boolean isAutoTpaHere() {
        return autoTpaHere;
    }

    public void setAutoTpaHere(boolean autoTpaHere) {
        this.autoTpaHere = autoTpaHere;
    }

    public boolean isAutoFriendTp() {
        return autoFriendTp;
    }

    public void setAutoFriendTp(boolean autoFriendTp) {
        this.autoFriendTp = autoFriendTp;
    }

    public boolean isFriendJoinNotify() {
        return friendJoinNotify;
    }

    public void setFriendJoinNotify(boolean friendJoinNotify) {
        this.friendJoinNotify = friendJoinNotify;
    }

    // ═══════════════════════════════════════
    // Staff States
    // ═══════════════════════════════════════
    public boolean isFlyEnabled() {
        return flyEnabled;
    }

    public void setFlyEnabled(boolean flyEnabled) {
        this.flyEnabled = flyEnabled;
    }

    public boolean isGodEnabled() {
        return godEnabled;
    }

    public void setGodEnabled(boolean godEnabled) {
        this.godEnabled = godEnabled;
    }

    // ═══════════════════════════════════════
    // Economy
    // ═══════════════════════════════════════
    public long getBalance() {
        return balance;
    }

    public void setBalance(long balance) {
        this.balance = Math.max(0, balance);
    }

    public void addBalance(long amount) {
        this.balance = Math.max(0, this.balance + amount);
    }

    public void removeBalance(long amount) {
        this.balance = Math.max(0, this.balance - amount);
    }

    public boolean hasBalance(long amount) {
        return this.balance >= amount;
    }

    // ═══════════════════════════════════════
    // Message Reply
    // ═══════════════════════════════════════
    public UUID getLastMessageFrom() {
        return lastMessageFrom;
    }

    public void setLastMessageFrom(UUID lastMessageFrom) {
        this.lastMessageFrom = lastMessageFrom;
    }

    public boolean hasLastMessage() {
        return lastMessageFrom != null;
    }

    // ═══════════════════════════════════════
    // Toggle Helper (untuk SettingsGUI)
    // ═══════════════════════════════════════

    public boolean toggle(String setting) {
        switch (setting.toLowerCase()) {
            case "tpa" -> {
                tpaEnabled = !tpaEnabled;
                return tpaEnabled;
            }
            case "msg" -> {
                msgEnabled = !msgEnabled;
                return msgEnabled;
            }
            case "sound" -> {
                soundEnabled = !soundEnabled;
                return soundEnabled;
            }
            case "randomtpa" -> {
                randomTpaEnabled = !randomTpaEnabled;
                return randomTpaEnabled;
            }
            case "privacy" -> {
                privacyMode = !privacyMode;
                return privacyMode;
            }
            case "autotpa" -> {
                autoTpa = !autoTpa;
                return autoTpa;
            }
            case "autotpahere" -> {
                autoTpaHere = !autoTpaHere;
                return autoTpaHere;
            }
            case "autofriendtp" -> {
                autoFriendTp = !autoFriendTp;
                return autoFriendTp;
            }
            case "friendjoinnotify" -> {
                friendJoinNotify = !friendJoinNotify;
                return friendJoinNotify;
            }
            case "fly" -> {
                flyEnabled = !flyEnabled;
                return flyEnabled;
            }
            case "god" -> {
                godEnabled = !godEnabled;
                return godEnabled;
            }
            default -> {
                return false;
            }
        }
    }

    public boolean getSetting(String setting) {
        return switch (setting.toLowerCase()) {
            case "tpa" -> tpaEnabled;
            case "msg" -> msgEnabled;
            case "sound" -> soundEnabled;
            case "randomtpa" -> randomTpaEnabled;
            case "privacy" -> privacyMode;
            case "autotpa" -> autoTpa;
            case "autotpahere" -> autoTpaHere;
            case "autofriendtp" -> autoFriendTp;
            case "friendjoinnotify" -> friendJoinNotify;
            case "fly" -> flyEnabled;
            case "god" -> godEnabled;
            default -> false;
        };
    }

    public void resetSettings() {
        this.tpaEnabled = true;
        this.msgEnabled = true;
        this.soundEnabled = true;
        this.randomTpaEnabled = true;
        this.privacyMode = false;
        this.autoTpa = false;
        this.autoTpaHere = false;
        this.autoFriendTp = false;
        this.friendJoinNotify = true;
    }

    public void resetStaffStates() {
        this.flyEnabled = false;
        this.godEnabled = false;
    }
}