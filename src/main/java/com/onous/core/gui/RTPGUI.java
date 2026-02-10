package com.onous.core.gui;

import com.onous.core.OnousCore;
import com.onous.core.util.CC;
import com.onous.core.util.ItemBuilder;
import com.onous.core.util.Sounds;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/**
 * RTP GUI - DonutSMP Style (3 rows, clean, elegant)
 *
 * Layout (3 rows = 27):
 *  Row 0: slot 4  → Header (Compass)
 *  Row 1: slot 10 → Overworld, slot 13 → Nether, slot 16 → End
 *  Row 2: slot 22 → Close (Barrier)
 */
public class RTPGUI extends GUI {

    // Slots
    private static final int SLOT_HEADER    = 4;
    private static final int SLOT_OVERWORLD = 10;
    private static final int SLOT_NETHER    = 13;
    private static final int SLOT_END       = 16;
    private static final int SLOT_CLOSE     = 22;

    public RTPGUI(OnousCore plugin, Player player) {
        super(plugin, player, CC.PRIMARY + "Random Teleport", 3);
    }

    @Override
    public void setup() {
        // Cooldown info
        boolean onCd = plugin.getCooldown().isOnCooldown(player.getUniqueId(), "rtp")
                && !player.hasPermission("onous.bypass.cooldown");
        long cdRemain = plugin.getCooldown().getRemaining(player.getUniqueId(), "rtp");
        int cdSec = plugin.getConfig().getInt("rtp.cooldown", 300);

        // Enabled worlds
        boolean owEnabled  = plugin.getConfig().getBoolean("rtp.worlds.overworld", true);
        boolean ntEnabled  = plugin.getConfig().getBoolean("rtp.worlds.nether", true);
        boolean endEnabled = plugin.getConfig().getBoolean("rtp.worlds.end", false);

        // Range
        int minRange = plugin.getConfig().getInt("rtp.min-range", 1000);
        int maxRange = plugin.getConfig().getInt("rtp.max-range", 8000);

        // Header (center)
        setItem(SLOT_HEADER,
                new ItemBuilder(Material.COMPASS)
                        .name(CC.PRIMARY + "Random Teleport")
                        .lore(
                                "",
                                CC.GRAY + "Teleport to a random safe location.",
                                CC.DARK + "Range: " + CC.WHITE + minRange + CC.GRAY + " - " + CC.WHITE + maxRange,
                                CC.DARK + "Cooldown: " + CC.WHITE + cdSec + "s",
                                ""
                        )
                        .glow()
                        .build()
        );

        // Overworld
        setItem(SLOT_OVERWORLD, buildWorldButton(
                World.Environment.NORMAL,
                "Overworld",
                Material.GRASS_BLOCK,
                owEnabled,
                onCd,
                cdRemain
        ));

        // Nether
        setItem(SLOT_NETHER, buildWorldButton(
                World.Environment.NETHER,
                "Nether",
                Material.NETHERRACK,
                ntEnabled,
                onCd,
                cdRemain
        ));

        // End
        setItem(SLOT_END, buildWorldButton(
                World.Environment.THE_END,
                "The End",
                Material.END_STONE,
                endEnabled,
                onCd,
                cdRemain
        ));

        // Close (bottom center)
        setItem(SLOT_CLOSE,
                new ItemBuilder(Material.BARRIER)
                        .name(CC.ERROR + "Close")
                        .build()
        );
    }

    @Override
    public void onClick(int slot) {
        switch (slot) {
            case SLOT_OVERWORLD -> tryRTP(World.Environment.NORMAL, "overworld");
            case SLOT_NETHER    -> tryRTP(World.Environment.NETHER, "nether");
            case SLOT_END       -> tryRTP(World.Environment.THE_END, "end");
            case SLOT_CLOSE     -> {
                close();
                Sounds.click(player);
            }
        }
    }

    // Build one world button in “Donut” vibe
    private org.bukkit.inventory.ItemStack buildWorldButton(World.Environment env,
                                                            String name,
                                                            Material icon,
                                                            boolean enabled,
                                                            boolean onCooldown,
                                                            long cdRemainMs) {
        if (!enabled) {
            return new ItemBuilder(Material.BARRIER)
                    .name(CC.ERROR + name + " Disabled")
                    .lore(
                            "",
                            CC.GRAY + "RTP in this world",
                            CC.GRAY + "is currently disabled.",
                            ""
                    )
                    .build();
        }

        if (onCooldown) {
            return new ItemBuilder(Material.CLOCK)
                    .name(CC.GRAY + name)
                    .lore(
                            "",
                            CC.ERROR + "On cooldown: " + CC.WHITE + formatSeconds(cdRemainMs / 1000),
                            CC.DARK + progressBar(cdRemainMs),
                            ""
                    )
                    .build();
        }

        // Ready state
        String color = switch (env) {
            case NORMAL -> CC.SUCCESS;
            case NETHER -> CC.ERROR;
            case THE_END -> CC.ACCENT;
            default -> CC.WHITE;
        };

        return new ItemBuilder(icon)
                .name(color + name)
                .lore(
                        "",
                        CC.GRAY + "• Safe ground",
                        CC.GRAY + "• Air space",
                        CC.GRAY + "• No lava/magma",
                        "",
                        CC.DARK + "Click to teleport"
                )
                .glow()
                .build();
    }

    private void tryRTP(World.Environment env, String configKey) {
        // Enabled?
        if (!plugin.getConfig().getBoolean("rtp.worlds." + configKey, false)) {
            CC.sendPrefixed(player, plugin.getMessage("rtp-disabled-world"));
            Sounds.error(player);
            return;
        }

        // Cooldown?
        if (plugin.getCooldown().isOnCooldown(player.getUniqueId(), "rtp")
                && !player.hasPermission("onous.bypass.cooldown")) {
            long remaining = plugin.getCooldown().getRemaining(player.getUniqueId(), "rtp");
            String msg = plugin.getMessage("cooldown").replace("{time}", String.valueOf(remaining));
            CC.sendPrefixed(player, msg);
            Sounds.error(player);
            return;
        }

        // World
        World world = findWorld(env);
        if (world == null) {
            CC.sendPrefixed(player, plugin.getMessage("rtp-disabled-world"));
            Sounds.error(player);
            return;
        }

        // Close GUI & start async search
        close();
        CC.sendPrefixed(player, plugin.getMessage("rtp-searching"));
        Sounds.click(player);

        new BukkitRunnable() {
            @Override
            public void run() {
                Location safe = findSafeLocation(world, env);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (!player.isOnline()) return;

                        if (safe != null) {
                            // Teleport with delay (uses plugin’s TeleportManager)
                            plugin.getTeleport().teleport(player, safe, () ->
                                    CC.sendPrefixed(player, plugin.getMessage("rtp-success"))
                            );

                            int cooldown = plugin.getConfig().getInt("rtp.cooldown", 300);
                            plugin.getCooldown().set(player.getUniqueId(), "rtp", cooldown);
                        } else {
                            CC.sendPrefixed(player, plugin.getMessage("rtp-failed"));
                            Sounds.error(player);
                        }
                    }
                }.runTask(plugin);
            }
        }.runTaskAsynchronously(plugin);
    }

    private World findWorld(World.Environment env) {
        for (World w : plugin.getServer().getWorlds()) {
            if (w.getEnvironment() == env) return w;
        }
        return null;
    }

    private Location findSafeLocation(World world, World.Environment env) {
        Random random = new Random();
        int minRange = plugin.getConfig().getInt("rtp.min-range", 1000);
        int maxRange = plugin.getConfig().getInt("rtp.max-range", 8000);
        int maxAttempts = plugin.getConfig().getInt("rtp.max-attempts", 15);

        for (int i = 0; i < maxAttempts; i++) {
            int x = randomCoord(random, minRange, maxRange);
            int z = randomCoord(random, minRange, maxRange);

            Location safe = findSafeY(world, x, z, env);
            if (safe != null) return safe;
        }
        return null;
    }

    private int randomCoord(Random r, int min, int max) {
        int v = r.nextInt(max - min) + min;
        return r.nextBoolean() ? v : -v;
    }

    private Location findSafeY(World world, int x, int z, World.Environment env) {
        int minY = (env == World.Environment.NETHER) ? 32 : world.getMinHeight() + 10;
        int maxY = (env == World.Environment.NETHER) ? 100 : world.getMaxHeight() - 20;

        for (int y = maxY; y >= minY; y--) {
            Block ground = world.getBlockAt(x, y - 1, z);
            Block feet   = world.getBlockAt(x, y, z);
            Block head   = world.getBlockAt(x, y + 1, z);

            if (!ground.getType().isSolid()) continue;
            if (isDangerous(ground.getType())) continue;
            if (!feet.getType().isAir()) continue;
            if (!head.getType().isAir()) continue;

            return new Location(world, x + 0.5, y, z + 0.5);
        }
        return null;
    }

    private boolean isDangerous(Material mat) {
        return switch (mat) {
            case LAVA, WATER, FIRE, SOUL_FIRE, MAGMA_BLOCK, CACTUS,
                 SWEET_BERRY_BUSH, POWDER_SNOW, POINTED_DRIPSTONE -> true;
            default -> false;
        };
    }

    // Pretty time for cooldown
    private String formatSeconds(long s) {
        if (s <= 0) return "0s";
        long m = s / 60;
        long sec = s % 60;
        return (m > 0 ? m + "m " : "") + sec + "s";
    }

    // Simple ASCII progress bar (visual only)
    private String progressBar(long remainMs) {
        // Convert to 10 segments (visual)
        long remainSec = Math.max(0, remainMs / 1000);
        long total = plugin.getConfig().getInt("rtp.cooldown", 300);
        if (total <= 0) total = 1;

        double ratio = Math.min(1.0, (double) remainSec / total);
        int filled = (int) Math.round((10 * ratio));
        int empty = 10 - filled;

        StringBuilder sb = new StringBuilder();
        sb.append(CC.ERROR);
        for (int i = 0; i < filled; i++) sb.append("▌");
        sb.append(CC.DARK);
        for (int i = 0; i < empty; i++) sb.append("▌");
        return CC.translate(sb.toString());
    }
}