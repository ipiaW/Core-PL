package com.onous.core.gui;

import com.onous.core.OnousCore;
import com.onous.core.data.PlayerData;
import com.onous.core.util.CC;
import com.onous.core.util.ItemBuilder;
import com.onous.core.util.Sounds;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Settings GUI - 3 Rows Minimalist
 *
 * Row 0: [0-8]
 *   2 → TPA
 *   3 → Messages
 *   4 → Sounds
 *   5 → Random TPA
 *   6 → Privacy
 *
 * Row 1: [9-17]
 *   10 → Auto TPA
 *   12 → Auto Friend TP
 *   14 → Auto TPAHere
 *   16 → Friend Join Notify
 *
 * Row 2: [18-26]
 *   22 → Close
 */
public class SettingsGUI extends GUI {

    public SettingsGUI(OnousCore plugin, Player player) {
        super(plugin, player, CC.PRIMARY + "Settings", 3);
    }

    @Override
    public void setup() {
        PlayerData d = plugin.getData().getSettings(player.getUniqueId());

        // Row 0 - basic toggles
        setItem(2, createToggle("TPA", d.isTpaEnabled(), "Receive teleport requests"));
        setItem(3, createToggle("Messages", d.isMsgEnabled(), "Receive private messages"));
        setItem(4, createToggle("Sounds", d.isSoundEnabled(), "Play GUI and notify sounds"));
        setItem(5, createToggle("Random TPA", d.isRandomTpaEnabled(), "Appear in random TPA"));
        setItem(6, createToggle("Privacy", d.isPrivacyMode(), "Hide from random selections"));

        // Row 1 - advanced toggles
        setItem(10, createToggle("Auto TPA", d.isAutoTpa(),
                "Auto-accept all /tpa", "requests to you"));
        setItem(12, createToggle("Auto Friend TP", d.isAutoFriendTp(),
                "Auto-accept /tpa", "from your friends only"));
        setItem(14, createToggle("Auto TPAHere", d.isAutoTpaHere(),
                "Auto-accept /tpahere", "requests to you"));
        setItem(16, createToggle("Friend Join Notify", d.isFriendJoinNotify(),
                "Show actionbar when", "friends join the server"));

        // Row 2 - close
        setItem(22, new ItemBuilder(Material.BARRIER)
                .name(CC.ERROR + "Close")
                .build());
    }

    @Override
    public void onClick(int slot) {
        PlayerData d = plugin.getData().getSettings(player.getUniqueId());

        switch (slot) {
            case 2 -> toggle(d, "tpa");
            case 3 -> toggle(d, "msg");
            case 4 -> toggle(d, "sound");
            case 5 -> toggle(d, "randomtpa");
            case 6 -> toggle(d, "privacy");

            case 10 -> toggle(d, "autotpa");
            case 12 -> toggle(d, "autofriendtp");
            case 14 -> toggle(d, "autotpahere");
            case 16 -> toggle(d, "friendjoinnotify");

            case 22 -> {
                close();
                Sounds.click(player);
            }
        }
    }

    private void toggle(PlayerData data, String key) {
        boolean newState = data.toggle(key);
        if (newState) {
            Sounds.toggleOn(player);
        } else {
            Sounds.toggleOff(player);
        }
        refresh();
    }

    private ItemStack createToggle(String name, boolean enabled, String... descLines) {
        Material mat = enabled ? Material.LIME_DYE : Material.GRAY_DYE;
        String status = enabled ? CC.SUCCESS + "ON" : CC.ERROR + "OFF";

        ItemBuilder builder = new ItemBuilder(mat)
                .name(CC.PRIMARY + name + " " + status);

        if (descLines != null && descLines.length > 0) {
            String[] lore = new String[descLines.length + 2];
            int i = 0;
            for (String line : descLines) {
                lore[i++] = CC.GRAY + line;
            }
            lore[i++] = "";
            lore[i] = CC.DARK + "Click to toggle";
            builder.lore(lore);
        } else {
            builder.lore(
                    "",
                    CC.DARK + "Click to toggle"
            );
        }

        return builder.build();
    }
}