package com.onous.core.command;

import com.onous.core.OnousCore;
import com.onous.core.gui.BalanceGUI;
import com.onous.core.util.CC;
import com.onous.core.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BalanceCommand implements CommandExecutor {

    private final OnousCore plugin;

    public BalanceCommand(OnousCore plugin) {
        this.plugin = plugin;
        plugin.getCommand("balance").setExecutor(this); // Alias: bal, money diatur di plugin.yml
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            // Console check balance
            if (args.length > 0) {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
                if (target.hasPlayedBefore() || target.isOnline()) {
                    double bal = plugin.getEcoManager().getBalance(target);
                    CC.send(sender, "&e" + target.getName() + "'s Balance: &f" + plugin.getEcoManager().format(bal));
                } else {
                    CC.send(sender, "&cPlayer not found!");
                }
            } else {
                CC.send(sender, "&cUsage: /balance <player>");
            }
            return true;
        }

        // GUI Mode (No args)
        if (args.length == 0) {
            new BalanceGUI(plugin, player).open();
            return true;
        }

        // Check Other Player
        if (!player.hasPermission("onous.balance.others")) {
            CC.sendPrefixed(player, "&cYou don't have permission to check others balance.");
            return true;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
        if (!target.hasPlayedBefore() && !target.isOnline()) {
            CC.sendPrefixed(player, "&cPlayer never played before.");
            return true;
        }

        double bal = plugin.getEcoManager().getBalance(target);
        CC.sendPrefixed(player, "&e" + target.getName() + "'s Balance: &a" + plugin.getEcoManager().format(bal));
        Sounds.click(player);
        
        return true;
    }
}
