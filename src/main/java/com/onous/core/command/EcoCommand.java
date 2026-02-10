package com.onous.core.command;

import com.onous.core.OnousCore;
import com.onous.core.util.CC;
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

public class EcoCommand implements CommandExecutor, TabCompleter {

    private final OnousCore plugin;

    public EcoCommand(OnousCore plugin) {
        this.plugin = plugin;
        plugin.getCommand("eco").setExecutor(this);
        plugin.getCommand("eco").setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("onous.admin")) {
            CC.send(sender, "&cNo permission.");
            return true;
        }

        if (args.length < 3) {
            CC.send(sender, "&cUsage: /eco <give|take|set> <player> <amount>");
            return true;
        }

        String action = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            CC.send(sender, "&cPlayer not found.");
            return true;
        }

        // ✅ Gunakan parser baru
        double amount = plugin.getEcoManager().parseAmount(args[2]);

        if (amount == -1) {
            CC.send(sender, "&cInvalid amount format! Use: 100, 10k, 1.5m");
            return true;
        }

        if (amount < 0) {
            CC.send(sender, "&cAmount cannot be negative.");
            return true;
        }

        switch (action) {
            case "give", "add" -> {
                plugin.getEcoManager().deposit(target, amount);
                CC.send(sender, "&aAdded &f" + plugin.getEcoManager().format(amount) + " &ato " + target.getName());
            }
            case "take", "remove" -> {
                plugin.getEcoManager().withdraw(target, amount);
                CC.send(sender, "&aTook &f" + plugin.getEcoManager().format(amount) + " &afrom " + target.getName());
            }
            case "set" -> {
                plugin.getEcoManager().setBalance(target, amount);
                CC.send(sender, "&aSet " + target.getName() + "'s balance to &f" + plugin.getEcoManager().format(amount));
            }
            default -> CC.send(sender, "&cUnknown action. Use give, take, or set.");
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("give", "take", "set");
        }
        if (args.length == 2) {
            String input = args[1].toLowerCase();
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
        }
        if (args.length == 3) {
            return List.of("100", "1k", "10k", "1m", "1b");
        }
        return new ArrayList<>();
    }
}