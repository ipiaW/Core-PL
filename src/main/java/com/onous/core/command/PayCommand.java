package com.onous.core.command;

import com.onous.core.OnousCore;
import com.onous.core.data.PlayerData;
import com.onous.core.gui.PayConfirmGUI;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PayCommand implements CommandExecutor, TabCompleter {

    private final OnousCore plugin;

    public PayCommand(OnousCore plugin) {
        this.plugin = plugin;
        plugin.getCommand("pay").setExecutor(this);
        plugin.getCommand("pay").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            CC.send(sender, "&cPlayers only!");
            return true;
        }

        if (args.length < 2) {
            CC.sendPrefixed(player, "&cUsage: /pay <player> <amount>");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            CC.sendPrefixed(player, "&cPlayer not found.");
            return true;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            CC.sendPrefixed(player, "&cYou cannot pay yourself.");
            return true;
        }

        // ✅ Gunakan parser baru
        double amount = plugin.getEcoManager().parseAmount(args[1]);

        if (amount == -1) {
            CC.sendPrefixed(player, "&cInvalid amount format! Use: 100, 10k, 1.5m");
            return true;
        }

        if (amount <= 0) {
            CC.sendPrefixed(player, "&cAmount must be positive.");
            return true;
        }

        if (!plugin.getEcoManager().has(player, amount)) {
            CC.sendPrefixed(player, "&cInsufficient funds.");
            Sounds.error(player);
            return true;
        }

        new PayConfirmGUI(plugin, player, target, amount).open();
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            return List.of("100", "1k", "10k", "100k", "1m");
        }
        return new ArrayList<>();
    }
}