package com.onous.core.gui;

import com.onous.core.OnousCore;
import com.onous.core.util.CC;
import com.onous.core.util.ItemBuilder;
import com.onous.core.util.Sounds;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

public class PayConfirmGUI extends GUI {

    private final OfflinePlayer target;
    private final double amount;

    public PayConfirmGUI(OnousCore plugin, Player player, OfflinePlayer target, double amount) {
        super(plugin, player, "&8Confirm Payment", 3);
        this.target = target;
        this.amount = amount;
    }

    @Override
    public void setup() {
        // Confirm Button (Green)
        setItem(11, new ItemBuilder(Material.LIME_WOOL)
                .name("&a&lCONFIRM")
                .lore("&7Send &e" + plugin.getEcoManager().format(amount), "&7to &f" + target.getName())
                .build());

        // Info (Center)
        setItem(13, new ItemBuilder(Material.PAPER)
                .name("&ePayment Info")
                .lore("&7Amount: &f" + plugin.getEcoManager().format(amount), "&7Target: &f" + target.getName())
                .build());

        // Cancel Button (Red)
        setItem(15, new ItemBuilder(Material.RED_WOOL)
                .name("&c&lCANCEL")
                .lore("&7Cancel transaction")
                .build());
    }

    @Override
    public void onClick(int slot) {
        if (slot == 11) { // Confirm
            if (plugin.getEcoManager().has(player, amount)) {
                plugin.getEcoManager().withdraw(player, amount);
                plugin.getEcoManager().deposit(target, amount);
                
                CC.sendPrefixed(player, "&aSent &f" + plugin.getEcoManager().format(amount) + " &ato " + target.getName());
                if (target.isOnline()) {
                    CC.sendPrefixed(target.getPlayer(), "&aReceived &f" + plugin.getEcoManager().format(amount) + " &afrom " + player.getName());
                    Sounds.success(target.getPlayer());
                }
                Sounds.success(player);
            } else {
                CC.sendPrefixed(player, "&cInsufficient funds.");
                Sounds.error(player);
            }
            close();
        } else if (slot == 15) { // Cancel
            CC.sendPrefixed(player, "&cPayment cancelled.");
            Sounds.click(player);
            close();
        }
    }
}