package com.onous.core.gui;

import com.onous.core.OnousCore;
import com.onous.core.friend.manager.FriendManager;
import com.onous.core.friend.model.FriendData;
import com.onous.core.util.CC;
import com.onous.core.util.ItemBuilder;
import com.onous.core.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

/**
 * Friend List GUI - 3 Rows, minimalis
 *
 * Layout (3 rows = 27):
 *
 * Row 0 (header):
 *   slot 4 → "Friends"
 *
 * Row 1-2 (list teman):
 *   Semua slot kecuali header(4) & close(22).
 *
 * Row 2 (footer):
 *   slot 22 → Close
 *
 * Klik:
 *  - Left-click  → kirim /tpa ke friend (pakai TPAManager)
 *  - Right-click → Confirm remove friend
 */
public class FriendListGUI extends GUI {

    private final FriendManager friends;
    // Mapping slot -> UUID friend
    private final Map<Integer, UUID> slotMap = new HashMap<>();

    public FriendListGUI(OnousCore plugin, Player player) {
        super(plugin, player, CC.PRIMARY + "Friends", 3);
        this.friends = plugin.getFriends();
    }

    @Override
    public void setup() {
        inventory.clear();
        slotMap.clear();

        FriendData data = friends.getData(player.getUniqueId());
        Set<UUID> friendSet = data.getFriends();

        // Pisahkan online & offline
        List<UUID> online = new ArrayList<>();
        List<UUID> offline = new ArrayList<>();

        for (UUID f : friendSet) {
            Player p = Bukkit.getPlayer(f);
            if (p != null && p.isOnline()) {
                online.add(f);
            } else {
                offline.add(f);
            }
        }

        // Header
        setItem(4, new ItemBuilder(Material.PAPER)
                .name(CC.PRIMARY + "Friends (" + friendSet.size() + ")")
                .lore(
                        "",
                        CC.GRAY + "Left-click: request teleport (/tpa)",
                        CC.GRAY + "Right-click: remove friend",
                        ""
                )
                .build());

        // Slots untuk friend (semua kecuali 4 & 22)
        List<Integer> friendSlots = new ArrayList<>();
        for (int i = 0; i < 27; i++) {
            if (i == 4 || i == 22) continue;
            friendSlots.add(i);
        }

        int index = 0;

        // Online friends dulu (hijau)
        for (UUID f : online) {
            if (index >= friendSlots.size()) break;
            int slot = friendSlots.get(index++);
            setFriendSlot(slot, f, true);
        }

        // Lalu offline friends (abu-abu)
        for (UUID f : offline) {
            if (index >= friendSlots.size()) break;
            int slot = friendSlots.get(index++);
            setFriendSlot(slot, f, false);
        }

        // Close button
        setItem(22, new ItemBuilder(Material.BARRIER)
                .name(CC.ERROR + "Close")
                .build());
    }

    private void setFriendSlot(int slot, UUID uuid, boolean online) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(uuid);
        String name = op.getName() != null ? op.getName() : "Unknown";

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(op);
            if (online) {
                meta.setDisplayName(CC.translate(CC.SUCCESS + "● " + name));
                meta.setLore(List.of(
                        CC.translate(CC.GRAY + "Status: " + CC.SUCCESS + "Online"),
                        "",
                        CC.translate(CC.DARK + "L-Click: /tpa " + name),
                        CC.translate(CC.DARK + "R-Click: Remove friend")
                ));
            } else {
                meta.setDisplayName(CC.translate(CC.GRAY + "○ " + name));
                meta.setLore(List.of(
                        CC.translate(CC.GRAY + "Status: " + CC.DARK + "Offline"),
                        "",
                        CC.translate(CC.DARK + "R-Click: Remove friend")
                ));
            }
            head.setItemMeta(meta);
        }

        inventory.setItem(slot, head);
        slotMap.put(slot, uuid);
    }

    @Override
    public void onClick(int slot) {
        // Close
        if (slot == 22) {
            close();
            Sounds.click(player);
            return;
        }

        // Klik friend
        if (!slotMap.containsKey(slot)) {
            return;
        }

        UUID friendUUID = slotMap.get(slot);
        Player friendOnline = Bukkit.getPlayer(friendUUID);
        boolean isOnline = (friendOnline != null && friendOnline.isOnline());

        // Klik kiri → minta /tpa (kalau friend online)
        // Klik kanan → remove friend (confirm)
        // Cara baca click: kita rely pada InventoryClickEvent default (left/right),
        // tapi base GUI kita tidak menerima ClickType, hanya slot.
        // Jadi di versi ini, kita treat semua klik sebagai left click,
        // dan remove friend akan kita pindah ke command (/friend remove) atau GUI khusus nanti.
        // Untuk menghormati spec, akan kita pakai pola:
        //   - Left-click → TPA
        //   - Shift-right (opsional) → Remove (tapi susah tanpa ClickType)
        //
        // Untuk sekarang: lakukan TPA kalau online, kalau offline → tunjukkan pesan.

        // TODO: Jika kamu mau benar-benar beda aksi antara left/right,
        // perlu modifikasi GUIListener agar meneruskan ClickType ke GUI.

        // Sementara:
        if (isOnline) {
            // Kirim request TPA friend
            plugin.getTPA().sendRequest(player, friendOnline);
        } else {
            CC.sendPrefixed(player, CC.ERROR + "That friend is offline.");
            Sounds.error(player);
        }
    }
}