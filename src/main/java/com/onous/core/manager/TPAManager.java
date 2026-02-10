package com.onous.core.manager;

import com.onous.core.OnousCore;
import com.onous.core.data.PlayerData;
import com.onous.core.gui.ConfirmGUI;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * TPA Manager
 *
 *  - /tpa <player>
 *  - /tpaccept
 *  - /tpdeny
 *
 * Fitur:
 *  - Cooldown
 *  - Expire request
 *  - Confirm GUI di target
 *  - Auto-accept jika:
 *      • target.autoTpa == true
 *      • ATAU (sender adalah friend & target.autoFriendTp == true)
 *  - cancelAll(UUID) untuk cleanup saat quit
 */
public class TPAManager {

    private final OnousCore plugin;
    private final Map<UUID, TPARequest> requests = new HashMap<>();

    public TPAManager(OnousCore plugin) {
        this.plugin = plugin;
    }

    // ═══════════════════════════════════════
    // Send Request
    // ═══════════════════════════════════════

    public boolean sendRequest(Player sender, Player target) {
        UUID s = sender.getUniqueId();
        UUID t = target.getUniqueId();

        if (s.equals(t)) {
            CC.sendPrefixed(sender, plugin.getMessage("tpa-self"));
            Sounds.error(sender);
            return false;
        }

        PlayerData targetData = plugin.getData().getSettings(t);

        if (!targetData.isTpaEnabled()) {
            CC.sendPrefixed(sender, plugin.getMessage("tpa-disabled"));
            Sounds.error(sender);
            return false;
        }

        if (requests.containsKey(s)) {
            CC.sendPrefixed(sender, plugin.getMessage("tpa-already-pending"));
            Sounds.error(sender);
            return false;
        }

        if (plugin.getCooldown().isOnCooldown(s, "tpa")
                && !sender.hasPermission("onous.bypass.cooldown")) {
            long remaining = plugin.getCooldown().getRemaining(s, "tpa");
            String msg = plugin.getMessage("cooldown").replace("{time}", String.valueOf(remaining));
            CC.sendPrefixed(sender, msg);
            Sounds.error(sender);
            return false;
        }

        int cd = plugin.getConfig().getInt("tpa.cooldown", 30);
        plugin.getCooldown().set(s, "tpa", cd);

        // Cek apakah sender adalah friend
        boolean isFriend = plugin.getFriends() != null
                && plugin.getFriends().areFriends(s, t);

        // AUTO ACCEPT jika:
        //  - target.autoTpa  = true, atau
        //  - (friend & target.autoFriendTp = true)
        if (targetData.isAutoTpa() || (isFriend && targetData.isAutoFriendTp())) {
            plugin.getTeleport().teleport(sender, target.getLocation(), () -> {
                CC.sendPrefixed(sender, plugin.getMessage("tpa-accepted"));
                CC.sendPrefixed(target, plugin.getMessage("tpa-accepted"));
            });
            Sounds.success(sender);
            Sounds.success(target);
            return true;
        }

        // NORMAL FLOW (GUI request)
        int expireSec = plugin.getConfig().getInt("tpa.expire", 60);

        BukkitTask expireTask = new BukkitRunnable() {
            @Override
            public void run() {
                TPARequest req = requests.remove(s);
                if (req != null) {
                    Player sp = Bukkit.getPlayer(s);
                    if (sp != null && sp.isOnline()) {
                        CC.sendPrefixed(sp, plugin.getMessage("tpa-expired"));
                        Sounds.error(sp);
                    }
                }
            }
        }.runTaskLater(plugin, expireSec * 20L);

        TPARequest request = new TPARequest(s, t, expireTask);
        requests.put(s, request);

        CC.sendPrefixed(sender, plugin.getMessage("tpa-sent").replace("{player}", target.getName()));

        new ConfirmGUI(
                plugin,
                target,
                CC.PRIMARY + "TPA Request",
                CC.WHITE + sender.getName() + CC.GRAY + " wants to teleport to you",
                () -> acceptLatest(target),
                () -> denyLatest(target)
        ).open();

        Sounds.notify(target);
        return true;
    }

    // ═══════════════════════════════════════
    // Accept / Deny
    // ═══════════════════════════════════════

    public void acceptLatest(Player target) {
        UUID t = target.getUniqueId();

        for (Map.Entry<UUID, TPARequest> e : requests.entrySet()) {
            TPARequest req = e.getValue();
            if (req.target().equals(t)) {
                acceptRequest(e.getKey(), target, req);
                return;
            }
        }

        CC.sendPrefixed(target, plugin.getMessage("tpa-no-pending"));
        Sounds.error(target);
    }

    public void denyLatest(Player target) {
        UUID t = target.getUniqueId();

        for (Map.Entry<UUID, TPARequest> e : requests.entrySet()) {
            TPARequest req = e.getValue();
            if (req.target().equals(t)) {
                denyRequest(e.getKey(), target, req);
                return;
            }
        }

        CC.sendPrefixed(target, plugin.getMessage("tpa-no-pending"));
        Sounds.error(target);
    }

    private void acceptRequest(UUID senderUUID, Player target, TPARequest req) {
        if (req == null || !req.target().equals(target.getUniqueId())) {
            CC.sendPrefixed(target, plugin.getMessage("tpa-no-pending"));
            Sounds.error(target);
            return;
        }

        Player sender = Bukkit.getPlayer(senderUUID);
        if (sender == null || !sender.isOnline()) {
            CC.sendPrefixed(target, plugin.getMessage("player-offline"));
            Sounds.error(target);
            cleanup(senderUUID, req);
            return;
        }

        cleanup(senderUUID, req);

        plugin.getTeleport().teleport(sender, target.getLocation(), () -> {
            CC.sendPrefixed(sender, plugin.getMessage("tpa-accepted"));
            CC.sendPrefixed(target, plugin.getMessage("tpa-accepted"));
        });

        Sounds.success(sender);
        Sounds.success(target);
    }

    private void denyRequest(UUID senderUUID, Player target, TPARequest req) {
        if (req == null || !req.target().equals(target.getUniqueId())) {
            CC.sendPrefixed(target, plugin.getMessage("tpa-no-pending"));
            Sounds.error(target);
            return;
        }

        Player sender = Bukkit.getPlayer(senderUUID);

        cleanup(senderUUID, req);

        if (sender != null && sender.isOnline()) {
            CC.sendPrefixed(sender, plugin.getMessage("tpa-denied"));
            Sounds.pop(sender);
        }

        CC.sendPrefixed(target, plugin.getMessage("tpa-denied"));
        Sounds.pop(target);
    }

    private void cleanup(UUID senderUUID, TPARequest req) {
        requests.remove(senderUUID);
        if (req.expireTask() != null) {
            req.expireTask().cancel();
        }
    }

    // ═══════════════════════════════════════
    // Cancel All (dipakai saat quit)
    // ═══════════════════════════════════════

    public void cancelAll(UUID playerUUID) {
        // Hapus request dikirim oleh player
        TPARequest asSender = requests.remove(playerUUID);
        if (asSender != null && asSender.expireTask() != null) {
            asSender.expireTask().cancel();
        }

        // Hapus request target = player
        requests.entrySet().removeIf(entry -> {
            TPARequest req = entry.getValue();
            if (req.target().equals(playerUUID)) {
                if (req.expireTask() != null) req.expireTask().cancel();
                return true;
            }
            return false;
        });
    }

    // ═══════════════════════════════════════
    // Data class
    // ═══════════════════════════════════════

    private record TPARequest(UUID sender, UUID target, BukkitTask expireTask) {}
}