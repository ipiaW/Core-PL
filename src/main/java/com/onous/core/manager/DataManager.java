package com.onous.core.manager;

import com.onous.core.OnousCore;
import com.onous.core.data.PlayerData;
import com.onous.core.data.Warp;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class DataManager {

    private final OnousCore plugin;

    private final Map<UUID, PlayerData> playerData;
    private final Map<String, Warp> warps;

    // Spawn cache + raw data
    private Location spawn;
    private String spawnWorldName;
    private double spawnX, spawnY, spawnZ;
    private float spawnYaw, spawnPitch;

    private final File playersFolder;
    private final File warpsFile;
    private final File spawnFile;

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");
    private static final long THOUSAND = 1_000L;
    private static final long MILLION = 1_000_000L;
    private static final long BILLION = 1_000_000_000L;
    private static final long TRILLION = 1_000_000_000_000L;

    public DataManager(OnousCore plugin) {
        this.plugin = plugin;
        this.playerData = new HashMap<>();
        this.warps = new HashMap<>();

        this.playersFolder = new File(plugin.getDataFolder(), "players");
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        this.spawnFile = new File(plugin.getDataFolder(), "spawn.yml");

        if (!playersFolder.exists()) {
            playersFolder.mkdirs();
        }

        loadWarps();
        loadSpawn();
    }

    // ═══════════════════════════════════════
    // Balance formatting
    // ═══════════════════════════════════════

    public String formatBalance(long amount) {
        String symbol = plugin.getConfig().getString("economy.symbol", "⛃");
        return symbol + " " + String.format("%,d", amount);
    }

    public String formatBalanceShort(long amount) {
        if (amount < THOUSAND) return String.valueOf(amount);
        if (amount < MILLION) return DECIMAL_FORMAT.format(amount / (double) THOUSAND) + "k";
        if (amount < BILLION) return DECIMAL_FORMAT.format(amount / (double) MILLION) + "M";
        if (amount < TRILLION) return DECIMAL_FORMAT.format(amount / (double) BILLION) + "B";
        return DECIMAL_FORMAT.format(amount / (double) TRILLION) + "T";
    }

    public String formatBalanceShortWithSymbol(long amount) {
        String symbol = plugin.getConfig().getString("economy.symbol", "⛃");
        return symbol + " " + formatBalanceShort(amount);
    }

    // ═══════════════════════════════════════
    // Player Data
    // ═══════════════════════════════════════

    public PlayerData getSettings(UUID uuid) {
        return playerData.computeIfAbsent(uuid, this::loadPlayer);
    }

    private PlayerData loadPlayer(UUID uuid) {
        PlayerData data = new PlayerData(uuid);
        File file = new File(playersFolder, uuid + ".yml");

        if (file.exists()) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

            data.setTpaEnabled(cfg.getBoolean("settings.tpa", true));
            data.setMsgEnabled(cfg.getBoolean("settings.msg", true));
            data.setSoundEnabled(cfg.getBoolean("settings.sound", true));
            data.setRandomTpaEnabled(cfg.getBoolean("settings.random-tpa", true));
            data.setPrivacyMode(cfg.getBoolean("settings.privacy", false));

            data.setAutoTpa(cfg.getBoolean("settings.auto-tpa", false));
            data.setAutoTpaHere(cfg.getBoolean("settings.auto-tpahere", false));
            data.setAutoFriendTp(cfg.getBoolean("settings.auto-friend-tp", false));
            data.setFriendJoinNotify(cfg.getBoolean("settings.friend-join-notify", true));

            data.setFlyEnabled(cfg.getBoolean("states.fly", false));
            data.setGodEnabled(cfg.getBoolean("states.god", false));

            long startingBalance = plugin.getConfig().getLong("economy.starting-balance", 1000);
            data.setBalance(cfg.getLong("economy.balance", startingBalance));

            data.setLastName(cfg.getString("info.last-name"));
        } else {
            long startingBalance = plugin.getConfig().getLong("economy.starting-balance", 1000);
            data.setBalance(startingBalance);
        }

        return data;
    }

    public void savePlayer(UUID uuid) {
        PlayerData data = playerData.get(uuid);
        if (data == null) return;

        File file = new File(playersFolder, uuid + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();

        cfg.set("settings.tpa", data.isTpaEnabled());
        cfg.set("settings.msg", data.isMsgEnabled());
        cfg.set("settings.sound", data.isSoundEnabled());
        cfg.set("settings.random-tpa", data.isRandomTpaEnabled());
        cfg.set("settings.privacy", data.isPrivacyMode());

        cfg.set("settings.auto-tpa", data.isAutoTpa());
        cfg.set("settings.auto-tpahere", data.isAutoTpaHere());
        cfg.set("settings.auto-friend-tp", data.isAutoFriendTp());
        cfg.set("settings.friend-join-notify", data.isFriendJoinNotify());

        cfg.set("states.fly", data.isFlyEnabled());
        cfg.set("states.god", data.isGodEnabled());

        cfg.set("economy.balance", data.getBalance());

        cfg.set("info.last-name", data.getLastName());

        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save player data: " + uuid);
        }
    }

    public void unloadPlayer(UUID uuid) {
        savePlayer(uuid);
        playerData.remove(uuid);
    }

    // ═══════════════════════════════════════
    // Warps
    // ═══════════════════════════════════════

    public Warp getWarp(String name) {
        return warps.get(name.toLowerCase());
    }

    public Collection<Warp> getWarps() {
        return warps.values();
    }

    public boolean warpExists(String name) {
        return warps.containsKey(name.toLowerCase());
    }

    public void createWarp(String name, Location location) {
        Warp warp = new Warp(name, location);
        warps.put(name.toLowerCase(), warp);
        saveWarps();
    }

    public void createWarp(String name, Location location, Material icon) {
        Warp warp = new Warp(name, location, icon);
        warps.put(name.toLowerCase(), warp);
        saveWarps();
    }

    public void deleteWarp(String name) {
        warps.remove(name.toLowerCase());
        saveWarps();
    }

    public int getWarpCount() {
        return warps.size();
    }

    private void loadWarps() {
        warps.clear();
        if (!warpsFile.exists()) return;

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(warpsFile);

        for (String name : cfg.getKeys(false)) {
            String path = name + ".";

            String worldName = cfg.getString(path + "world");
            if (worldName == null || Bukkit.getWorld(worldName) == null) {
                plugin.getLogger().warning("Warp '" + name + "' has invalid world, skipping...");
                continue;
            }

            Location loc = new Location(
                    Bukkit.getWorld(worldName),
                    cfg.getDouble(path + "x"),
                    cfg.getDouble(path + "y"),
                    cfg.getDouble(path + "z"),
                    (float) cfg.getDouble(path + "yaw"),
                    (float) cfg.getDouble(path + "pitch")
            );

            Warp warp = new Warp(name, loc);

            String iconName = cfg.getString(path + "icon", "ENDER_PEARL");
            try {
                warp.setIcon(Material.valueOf(iconName.toUpperCase()));
            } catch (IllegalArgumentException ignored) {}

            String displayName = cfg.getString(path + "display-name");
            if (displayName != null) {
                warp.setDisplayName(displayName);
            }

            warps.put(name.toLowerCase(), warp);
        }

        plugin.getLogger().info("Loaded " + warps.size() + " warps.");
    }

    private void saveWarps() {
        YamlConfiguration cfg = new YamlConfiguration();

        for (Warp warp : warps.values()) {
            String path = warp.getName() + ".";
            Location loc = warp.getLocation();

            cfg.set(path + "world", loc.getWorld().getName());
            cfg.set(path + "x", loc.getX());
            cfg.set(path + "y", loc.getY());
            cfg.set(path + "z", loc.getZ());
            cfg.set(path + "yaw", loc.getYaw());
            cfg.set(path + "pitch", loc.getPitch());
            cfg.set(path + "icon", warp.getIcon().name());
            cfg.set(path + "display-name", warp.getDisplayName());
        }

        try {
            cfg.save(warpsFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save warps!");
        }
    }

    // ═══════════════════════════════════════
    // Spawn
    // ═══════════════════════════════════════

    public boolean hasSpawn() {
        return spawnWorldName != null;
    }

    public Location getSpawn() {
        if (spawn != null) return spawn.clone();
        return resolveSpawnLocation();
    }

    public void setSpawn(Location loc) {
        if (loc == null || loc.getWorld() == null) return;

        spawnWorldName = loc.getWorld().getName();
        spawnX = loc.getX();
        spawnY = loc.getY();
        spawnZ = loc.getZ();
        spawnYaw = loc.getYaw();
        spawnPitch = loc.getPitch();

        spawn = loc.clone();
        saveSpawn();
    }

    public void tryResolveSpawn() {
        resolveSpawnLocation();
    }

    private Location resolveSpawnLocation() {
        if (spawnWorldName == null) return null;
        World w = Bukkit.getWorld(spawnWorldName);
        if (w == null) return null;
        spawn = new Location(w, spawnX, spawnY, spawnZ, spawnYaw, spawnPitch);
        return spawn.clone();
    }

    private void loadSpawn() {
        if (!spawnFile.exists()) {
            spawnWorldName = null;
            spawn = null;
            return;
        }

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(spawnFile);

        spawnWorldName = cfg.getString("world");
        spawnX = cfg.getDouble("x");
        spawnY = cfg.getDouble("y");
        spawnZ = cfg.getDouble("z");
        spawnYaw = (float) cfg.getDouble("yaw");
        spawnPitch = (float) cfg.getDouble("pitch");

        Location resolved = resolveSpawnLocation();
        if (resolved != null) {
            plugin.getLogger().info("Spawn loaded: " + spawnWorldName);
        } else {
            plugin.getLogger().warning("Spawn loaded BUT world not loaded yet: " + spawnWorldName);
        }
    }

    private void saveSpawn() {
        if (spawnWorldName == null) return;

        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("world", spawnWorldName);
        cfg.set("x", spawnX);
        cfg.set("y", spawnY);
        cfg.set("z", spawnZ);
        cfg.set("yaw", spawnYaw);
        cfg.set("pitch", spawnPitch);

        try {
            cfg.save(spawnFile);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to save spawn!");
        }
    }

    // ═══════════════════════════════════════
    // Economy Leaderboard
    // ═══════════════════════════════════════

    public List<Map.Entry<UUID, Long>> getTopBalances(int limit) {
        List<Map.Entry<UUID, Long>> list = new ArrayList<>();

        for (Map.Entry<UUID, PlayerData> entry : playerData.entrySet()) {
            list.add(Map.entry(entry.getKey(), entry.getValue().getBalance()));
        }

        File[] files = playersFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                try {
                    UUID uuid = UUID.fromString(file.getName().replace(".yml", ""));
                    if (playerData.containsKey(uuid)) continue;

                    YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
                    long balance = cfg.getLong("economy.balance", 0);
                    list.add(Map.entry(uuid, balance));
                } catch (Exception ignored) {}
            }
        }

        list.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
        return list.subList(0, Math.min(limit, list.size()));
    }

    // ═══════════════════════════════════════
    // Save All
    // ═══════════════════════════════════════

    public void saveAll() {
        for (UUID uuid : playerData.keySet()) {
            savePlayer(uuid);
        }
        saveWarps();
        saveSpawn();
        plugin.getLogger().info("All data saved.");
    }
}