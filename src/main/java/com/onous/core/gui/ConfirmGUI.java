package com.onous.core.gui;

import com.onous.core.OnousCore;
import com.onous.core.util.CC;
import com.onous.core.util.ItemBuilder;
import com.onous.core.util.Sounds;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Confirm GUI - 3 Rows Minimalist
 */
public class ConfirmGUI extends GUI {

    private final String description;
    private final Runnable onAccept;
    private final Runnable onDeny;

    public ConfirmGUI(OnousCore plugin, Player player, String title, String description,
                      Runnable onAccept, Runnable onDeny) {
        super(plugin, player, title, 3);
        this.description = description;
        this.onAccept = onAccept;
        this.onDeny = onDeny;
    }

    @Override
    public void setup() {
        /*
         * Layout (3 rows = 27 slots):
         * 
         * Row 0: [0-8]   →  Accept(3), Info(4), Deny(5)
         * Row 1: [9-17]  →  Empty
         * Row 2: [18-26] →  Close(22)
         */

        // Row 0: Accept, Info, Deny
        setItem(3, new ItemBuilder(Material.LIME_DYE)
                .name(CC.SUCCESS + "✓ Accept")
                .lore(
                        CC.GRAY + "Accept this request",
                        "",
                        CC.DARK + "Click to accept"
                )
                .build());

        setItem(4, new ItemBuilder(Material.PAPER)
                .name(CC.PRIMARY + "Request Info")
                .lore(CC.WHITE + description)
                .build());

        setItem(5, new ItemBuilder(Material.RED_DYE)
                .name(CC.ERROR + "✕ Deny")
                .lore(
                        CC.GRAY + "Deny this request",
                        "",
                        CC.DARK + "Click to deny"
                )
                .build());

        // Row 2: Close
        setItem(22, new ItemBuilder(Material.BARRIER)
                .name(CC.ERROR + "Close")
                .build());
    }

    @Override
    public void onClick(int slot) {
        switch (slot) {
            case 3 -> {
                close();
                Sounds.success(player);
                if (onAccept != null) onAccept.run();
            }
            case 5 -> {
                close();
                Sounds.pop(player);
                if (onDeny != null) onDeny.run();
            }
            case 22 -> {
                close();
                Sounds.click(player);
                if (onDeny != null) onDeny.run();
            }
        }
    }

    // Factory method for TPA
    public static ConfirmGUI tpaRequest(OnousCore plugin, Player target, Player sender,
                                         Runnable onAccept, Runnable onDeny) {
        return new ConfirmGUI(plugin, target,
                CC.PRIMARY + "TPA Request",
                sender.getName() + " wants to teleport to you",
                onAccept, onDeny);
    }
}