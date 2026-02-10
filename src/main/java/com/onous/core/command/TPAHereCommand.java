package com.onous.core.command;

import com.onous.core.OnousCore;
import com.onous.core.data.PlayerData;
import com.onous.core.gui.ConfirmGUI;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * /tpahere Command
 *
 *  - /tpahere <player>
 *      Kirim permintaan agar 1 player teleport ke kita.
 *      Target akan melihat Confirm GUI (Accept / Deny),
 *      KECUALI jika target mengaktifkan Auto TPAHere → auto accept.
 *
 *  - /tpahere *
 *      Teleport semua online player ke kita, TANPA Confirm GUI.
 *      Kecuali:
 *        • diri sendiri
 *        • player dengan permission onous.tpahere.exempt
 */
public class TPAHereCommand implements CommandExecutor, TabCompleter {

    private final OnousCore plugin;

    public TPAHereCommand(OnousCore plugin) {
        this.plugin = plugin;

        if (plugin.getCommand("tpahere") != null) {
            plugin.getCommand("tpahere").setExecutor(this);
            plugin.getCommand("tpahere").setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // Player only
        if (!(sender instanceof Player player)) {
            CC.sendPrefixed(sender, plugin.getMessage("player-only"));
            return true;
        }

        if (args.length < 1) {
            CC.sendPrefixed(player, CC.ERROR + "Usage: /tpahere <player|*>");
            Sounds.error(player);
            return true;
        }

        String arg = args[0];

        // /tpahere * → mass teleport without confirm
        if (arg.equals("*")) {
            return handleMass(player);
        }

        // /tpahere <player> → single request (auto-accept aware)
        return handleSingle(player, arg);
    }

    // ═══════════════════════════════════════
    // /tpahere *  → Mass Teleport
    // ═══════════════════════════════════════
    private boolean handleMass(Player sender) {
        if (!sender.hasPermission("onous.tpahere.all")) {
            CC.sendPrefixed(sender, plugin.getMessage("no-permission"));
            Sounds.error(sender);
            return true;
        }

        if (plugin.getCooldown().isOnCooldown(sender.getUniqueId(), "tpahere")
                && !sender.hasPermission("onous.bypass.cooldown")) {
            long remaining = plugin.getCooldown().getRemaining(sender.getUniqueId(), "tpahere");
            String msg = plugin.getMessage("cooldown").replace("{time}", String.valueOf(remaining));
            CC.sendPrefixed(sender, msg);
            Sounds.error(sender);
            return true;
        }

        Location dest = sender.getLocation().clone();
        int count = 0;

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(sender)) continue;
            if (p.hasPermission("onous.tpahere.exempt")) continue;

            plugin.getTeleport().teleportInstant(p, dest);
            CC.actionBar(p, CC.PRIMARY + "→ " + CC.WHITE + sender.getName());
            count++;
        }

        CC.sendPrefixed(sender, CC.SUCCESS + "Teleported " + CC.PRIMARY + count + CC.SUCCESS + " players to you.");
        Sounds.success(sender);

        int cd = Math.max(5, plugin.getConfig().getInt("cooldowns.tpahere", 30));
        plugin.getCooldown().set(sender.getUniqueId(), "tpahere", cd);
        return true;
    }

    // ═══════════════════════════════════════
    // /tpahere <player>  → Single Request
    // ═══════════════════════════════════════
    private boolean handleSingle(Player sender, String targetName) {
        if (!sender.hasPermission("onous.tpahere")) {
            CC.sendPrefixed(sender, plugin.getMessage("no-permission"));
            Sounds.error(sender);
            return true;
        }

        if (plugin.getCooldown().isOnCooldown(sender.getUniqueId(), "tpahere")
                && !sender.hasPermission("onous.bypass.cooldown")) {
            long remaining = plugin.getCooldown().getRemaining(sender.getUniqueId(), "tpahere");
            String msg = plugin.getMessage("cooldown").replace("{time}", String.valueOf(remaining));
            CC.sendPrefixed(sender, msg);
            Sounds.error(sender);
            return true;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !target.isOnline()) {
            CC.sendPrefixed(sender, plugin.getMessage("player-not-found"));
            Sounds.error(sender);
            return true;
        }

        if (target.equals(sender)) {
            CC.sendPrefixed(sender, CC.ERROR + "You cannot tpahere yourself!");
            Sounds.error(sender);
            return true;
        }

        Location senderLocSnapshot = sender.getLocation().clone();
        PlayerData targetData = plugin.getData().getSettings(target.getUniqueId());

        // AUTO ACCEPT: target mengaktifkan Auto TPAHere
        if (targetData.isAutoTpaHere()) {
            plugin.getTeleport().teleport(target, senderLocSnapshot, () -> {
                CC.sendPrefixed(target, CC.SUCCESS + "Teleported to " + CC.WHITE + sender.getName());
                CC.sendPrefixed(sender, CC.SUCCESS + target.getName() + CC.GRAY + " auto-accepted your request.");
            });
            Sounds.success(sender);
            Sounds.success(target);

            int cd = Math.max(5, plugin.getConfig().getInt("cooldowns.tpahere", 30));
            plugin.getCooldown().set(sender.getUniqueId(), "tpahere", cd);
            return true;
        }

        // NORMAL FLOW: pakai Confirm GUI
        new ConfirmGUI(
                plugin,
                target,
                CC.PRIMARY + "TPAHere Request",
                CC.WHITE + sender.getName() + CC.GRAY + " wants to teleport you to them",
                () -> {
                    // ON ACCEPT
                    plugin.getTeleport().teleport(target, senderLocSnapshot, () -> {
                        CC.sendPrefixed(target, CC.SUCCESS + "Teleported to " + CC.WHITE + sender.getName());
                        CC.sendPrefixed(sender, CC.SUCCESS + target.getName() + CC.GRAY + " accepted your request.");
                    });
                },
                () -> {
                    // ON DENY
                    CC.sendPrefixed(target, CC.ERROR + "You denied the request.");
                    CC.sendPrefixed(sender, CC.ERROR + target.getName() + CC.GRAY + " denied your request.");
                    Sounds.pop(sender);
                }
        ).open();

        Sounds.notify(target);
        CC.sendPrefixed(sender, CC.GRAY + "Request sent to " + CC.WHITE + target.getName());

        int cd = Math.max(5, plugin.getConfig().getInt("cooldowns.tpahere", 30));
        plugin.getCooldown().set(sender.getUniqueId(), "tpahere", cd);
        return true;
    }

    // ═══════════════════════════════════════
    // Tab Complete
    // ═══════════════════════════════════════
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player p)) return new ArrayList<>();

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> list = Bukkit.getOnlinePlayers().stream()
                    .filter(pl -> !pl.equals(p))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());

            if ("*".startsWith(input)) {
                list.add(0, "*");
            }
            return list;
        }

        return new ArrayList<>();
    }
}