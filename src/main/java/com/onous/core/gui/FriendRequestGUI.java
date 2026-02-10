package com.onous.core.gui;

import com.onous.core.OnousCore;
import com.onous.core.friend.manager.FriendManager;
import com.onous.core.util.CC;
import com.onous.core.util.ItemBuilder;
import com.onous.core.util.Sounds;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class FriendRequestGUI extends GUI {

    private final FriendManager friends;
    private final UUID requesterId;
    private final String requesterName;

    public FriendRequestGUI(OnousCore plugin, Player target, Player requester) {
        super(plugin, target, CC.PRIMARY + "Friend Request", 3);
        this.friends = plugin.getFriends();
        this.requesterId = requester.getUniqueId();
        this.requesterName = requester.getName();
    }

    @Override
    public void setup() {
        setItem(4, createRequesterHead());

        setItem(11, new ItemBuilder(Material.LIME_DYE)
                .name(CC.SUCCESS + "✓ Accept")
                .lore(
                        "",
                        CC.GRAY + "Add " + CC.WHITE + requesterName + CC.GRAY + " as your friend.",
                        "",
                        CC.DARK + "Click to accept"
                )
                .build());

        setItem(15, new ItemBuilder(Material.RED_DYE)
                .name(CC.ERROR + "✕ Deny")
                .lore(
                        "",
                        CC.GRAY + "Deny this friend request.",
                        "",
                        CC.DARK + "Click to deny"
                )
                .build());

        setItem(22, new ItemBuilder(Material.BARRIER)
                .name(CC.ERROR + "Close")
                .build());
    }

    private ItemStack createRequesterHead() {
        OfflinePlayer op = Bukkit.getOfflinePlayer(requesterId);
        String name = op.getName() != null ? op.getName() : requesterName;

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(op);
            meta.setDisplayName(CC.translate(CC.PRIMARY + name));
            meta.setLore(List.of(
                    CC.translate(""),
                    CC.translate(CC.GRAY + name + " sent you a friend request."),
                    CC.translate(""),
                    CC.translate(CC.GRAY + "Accept to add as friend."),
                    CC.translate("")
            ));
            head.setItemMeta(meta);
        }
        return head;
    }

    @Override
    public void onClick(int slot) {
        Player target = player;

        switch (slot) {
            case 11 -> {
                close();
                friends.acceptRequest(target, requesterId);
                Sounds.success(target);
            }
            case 15, 22 -> {
                close();
                friends.denyRequest(target, requesterId);
                Sounds.pop(target);
            }
        }
    }
}