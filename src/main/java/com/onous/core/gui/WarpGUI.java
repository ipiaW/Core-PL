package com.onous.core.gui;

import com.onous.core.OnousCore;
import com.onous.core.data.Warp;
import com.onous.core.util.CC;
import com.onous.core.util.ItemBuilder;
import com.onous.core.util.Sounds;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Warp GUI - 3 Rows Minimalist
 */
public class WarpGUI extends GUI {

    private final List<Warp> warps;
    private int page;
    
    private static final int ITEMS_PER_PAGE = 18;
    
    private static final int SLOT_PREV = 18;
    private static final int SLOT_CLOSE = 22;
    private static final int SLOT_NEXT = 26;

    public WarpGUI(OnousCore plugin, Player player) {
        this(plugin, player, 0);
    }

    public WarpGUI(OnousCore plugin, Player player, int page) {
        super(plugin, player, CC.PRIMARY + "Warps", 3);
        this.warps = new ArrayList<>(plugin.getData().getWarps());
        this.warps.sort(Comparator.comparing(Warp::getName));
        this.page = page;
    }

    @Override
    public void setup() {
        if (warps.isEmpty()) {
            setItem(4, new ItemBuilder(Material.BARRIER)
                    .name(CC.ERROR + "No Warps")
                    .lore(CC.GRAY + "No warps available")
                    .build());
            
            setItem(SLOT_CLOSE, closeButton());
            return;
        }

        int totalPages = getTotalPages(warps.size(), ITEMS_PER_PAGE);
        if (page >= totalPages) page = totalPages - 1;
        if (page < 0) page = 0;

        int start = page * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, warps.size());

        int slot = 0;
        for (int i = start; i < end && slot < ITEMS_PER_PAGE; i++) {
            setItem(slot, createWarpItem(warps.get(i)));
            slot++;
        }

        if (page > 0) {
            setItem(SLOT_PREV, new ItemBuilder(Material.ARROW)
                    .name(CC.PRIMARY + "← Previous")
                    .lore(CC.GRAY + "Page " + page + "/" + totalPages)
                    .build());
        }

        setItem(SLOT_CLOSE, closeButton());

        if (page < totalPages - 1) {
            setItem(SLOT_NEXT, new ItemBuilder(Material.ARROW)
                    .name(CC.PRIMARY + "Next →")
                    .lore(CC.GRAY + "Page " + (page + 2) + "/" + totalPages)
                    .build());
        }
    }

    @Override
    public void onClick(int slot) {
        int totalPages = getTotalPages(warps.size(), ITEMS_PER_PAGE);

        if (slot == SLOT_PREV && page > 0) {
            page--;
            Sounds.click(player);
            refresh();
            return;
        }

        if (slot == SLOT_NEXT && page < totalPages - 1) {
            page++;
            Sounds.click(player);
            refresh();
            return;
        }

        if (slot == SLOT_CLOSE) {
            close();
            Sounds.click(player);
            return;
        }

        if (slot >= 0 && slot < ITEMS_PER_PAGE) {
            int index = page * ITEMS_PER_PAGE + slot;
            if (index < warps.size()) {
                Warp warp = warps.get(index);
                close();
                
                String msg = plugin.getMessage("warp-teleporting")
                        .replace("{name}", warp.getDisplayName());
                CC.sendPrefixed(player, msg);
                
                plugin.getTeleport().teleport(player, warp.getLocation());
            }
        }
    }

    private ItemStack createWarpItem(Warp warp) {
        return new ItemBuilder(warp.getIcon())
                .name(CC.PRIMARY + warp.getDisplayName())
                .lore(
                        CC.GRAY + warp.getWorldName(),
                        CC.DARK + warp.getCoordinates()
                )
                .build();
    }

    private ItemStack closeButton() {
        return new ItemBuilder(Material.BARRIER)
                .name(CC.ERROR + "Close")
                .build();
    }
}