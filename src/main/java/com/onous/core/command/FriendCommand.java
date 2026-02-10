package com.onous.core.command;

import com.onous.core.OnousCore;
import com.onous.core.friend.manager.FriendManager;
import com.onous.core.friend.model.FriendData;
import com.onous.core.gui.FriendListGUI;
import com.onous.core.gui.FriendRequestGUI;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * /friend Command
 *
 *  /friend                       → buka GUI friend list
 *  /friend add <player>          → kirim friend request (GUI di target)
 *  /friend remove <player>       → remove friend (dua arah)
 *  /friend accept <player>       → accept friend request dari <player>
 *  /friend deny <player>         → deny friend request dari <player>
 */
public class FriendCommand implements CommandExecutor, TabCompleter {

    private final OnousCore plugin;
    private final FriendManager friends;

    public FriendCommand(OnousCore plugin) {
        this.plugin = plugin;
        this.friends = plugin.getFriends();

        if (plugin.getCommand("friend") != null) {
            plugin.getCommand("friend").setExecutor(this);
            plugin.getCommand("friend").setTabCompleter(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Hanya untuk player
        if (!(sender instanceof Player player)) {
            CC.sendPrefixed(sender, plugin.getMessage("player-only"));
            return true;
        }

        if (!player.hasPermission("onous.friend")) {
            CC.sendPrefixed(player, plugin.getMessage("no-permission"));
            Sounds.error(player);
            return true;
        }

        // /friend
        if (args.length == 0) {
            new FriendListGUI(plugin, player).open();
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "add" -> {
                if (args.length < 2) {
                    CC.sendPrefixed(player, CC.ERROR + "Usage: /friend add <player>");
                    Sounds.error(player);
                    return true;
                }
                handleAdd(player, args[1]);
                return true;
            }

            case "remove" -> {
                if (args.length < 2) {
                    CC.sendPrefixed(player, CC.ERROR + "Usage: /friend remove <player>");
                    Sounds.error(player);
                    return true;
                }
                handleRemove(player, args[1]);
                return true;
            }

            case "accept" -> {
                if (args.length < 2) {
                    CC.sendPrefixed(player, CC.ERROR + "Usage: /friend accept <player>");
                    Sounds.error(player);
                    return true;
                }
                handleAccept(player, args[1]);
                return true;
            }

            case "deny" -> {
                if (args.length < 2) {
                    CC.sendPrefixed(player, CC.ERROR + "Usage: /friend deny <player>");
                    Sounds.error(player);
                    return true;
                }
                handleDeny(player, args[1]);
                return true;
            }

            default -> {
                // Bantuan singkat
                CC.sendPrefixed(player, CC.GRAY + "Friend commands:");
                CC.sendPrefixed(player, CC.DARK + "  /friend" + CC.GRAY + " - open friend list");
                CC.sendPrefixed(player, CC.DARK + "  /friend add <player>" + CC.GRAY + " - send friend request");
                CC.sendPrefixed(player, CC.DARK + "  /friend remove <player>" + CC.GRAY + " - remove friend");
                CC.sendPrefixed(player, CC.DARK + "  /friend accept <player>" + CC.GRAY + " - accept friend request");
                CC.sendPrefixed(player, CC.DARK + "  /friend deny <player>" + CC.GRAY + " - deny friend request");
                return true;
            }
        }
    }

    // ═══════════════════════════════════════
    // /friend add <player>
    // ═══════════════════════════════════════

    private void handleAdd(Player sender, String targetName) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            CC.sendPrefixed(sender, plugin.getMessage("player-not-found"));
            Sounds.error(sender);
            return;
        }

        boolean ok = friends.sendRequest(sender, target);
        if (!ok) return;

        // Buka GUI Friend Request di target
        new FriendRequestGUI(plugin, target, sender).open();
        Sounds.notify(target);
    }

    // ═══════════════════════════════════════
    // /friend remove <player>
    // ═══════════════════════════════════════

    private void handleRemove(Player sender, String targetName) {
        // Untuk sekarang, remove hanya kalau target online (versi awal)
        Player target = Bukkit.getPlayer(targetName);

        if (target == null || !target.isOnline()) {
            CC.sendPrefixed(sender, CC.ERROR + "That player must be online to remove as friend (for now).");
            Sounds.error(sender);
            return;
        }

        friends.removeFriend(sender, target);
    }

    // ═══════════════════════════════════════
    // /friend accept <player>
    // ═══════════════════════════════════════

    private void handleAccept(Player player, String fromName) {
        FriendData data = friends.getData(player.getUniqueId());

        UUID found = null;
        for (UUID uuid : data.getRequests()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            String name = op.getName();
            if (name != null && name.equalsIgnoreCase(fromName)) {
                found = uuid;
                break;
            }
        }

        if (found == null) {
            CC.sendPrefixed(player, CC.ERROR + "No friend request from " + fromName + ".");
            Sounds.error(player);
            return;
        }

        friends.acceptRequest(player, found);
    }

    // ═══════════════════════════════════════
    // /friend deny <player>
    // ═══════════════════════════════════════

    private void handleDeny(Player player, String fromName) {
        FriendData data = friends.getData(player.getUniqueId());

        UUID found = null;
        for (UUID uuid : data.getRequests()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
            String name = op.getName();
            if (name != null && name.equalsIgnoreCase(fromName)) {
                found = uuid;
                break;
            }
        }

        if (found == null) {
            CC.sendPrefixed(player, CC.ERROR + "No friend request from " + fromName + ".");
            Sounds.error(player);
            return;
        }

        friends.denyRequest(player, found);
    }

    // ═══════════════════════════════════════
    // Tab Completer
    // ═══════════════════════════════════════

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }

        if (!player.hasPermission("onous.friend")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            String input = args[0].toLowerCase();
            List<String> sub = new ArrayList<>();
            if ("add".startsWith(input)) sub.add("add");
            if ("remove".startsWith(input)) sub.add("remove");
            if ("accept".startsWith(input)) sub.add("accept");
            if ("deny".startsWith(input)) sub.add("deny");
            return sub;
        }

        // /friend add <player>
        if (args.length == 2 && args[0].equalsIgnoreCase("add")) {
            String input = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !p.getName().equalsIgnoreCase(player.getName()))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
        }

        // /friend remove <player> → nama teman (online saja versi awal)
        if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            String input = args[1].toLowerCase();
            FriendData data = friends.getData(player.getUniqueId());

            return data.getFriends().stream()
                    .map(uuid -> Bukkit.getPlayer(uuid))
                    .filter(p -> p != null && p.isOnline())
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
        }

        // /friend accept <player> → nama pengirim request
        if (args.length == 2 && args[0].equalsIgnoreCase("accept")) {
            String input = args[1].toLowerCase();
            FriendData data = friends.getData(player.getUniqueId());

            return data.getRequests().stream()
                    .map(uuid -> Bukkit.getOfflinePlayer(uuid))
                    .map(OfflinePlayer::getName)
                    .filter(name -> name != null)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
        }

        // /friend deny <player> → nama pengirim request
        if (args.length == 2 && args[0].equalsIgnoreCase("deny")) {
            String input = args[1].toLowerCase();
            FriendData data = friends.getData(player.getUniqueId());

            return data.getRequests().stream()
                    .map(uuid -> Bukkit.getOfflinePlayer(uuid))
                    .map(OfflinePlayer::getName)
                    .filter(name -> name != null)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .sorted()
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}