package com.onous.core;

import com.onous.core.command.*;
import com.onous.core.economy.EcoManager;
import com.onous.core.economy.VaultHook;
import com.onous.core.friend.manager.FriendManager;
import com.onous.core.hook.PlaceholderHook;
import com.onous.core.listener.GUIListener;
import com.onous.core.listener.PlayerListener;
import com.onous.core.listener.TeleportListener;
import com.onous.core.listener.WorldListener;
import com.onous.core.manager.CooldownManager;
import com.onous.core.manager.DataManager;
import com.onous.core.manager.TPAManager;
import com.onous.core.manager.TeleportManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class OnousCore extends JavaPlugin {

    private static OnousCore instance;

    // Managers
    private DataManager dataManager;
    private TeleportManager teleportManager;
    private TPAManager tpaManager;
    private CooldownManager cooldownManager;
    private FriendManager friendManager;
    private EcoManager ecoManager;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        initManagers();
        registerCommands();
        registerListeners();
        registerPlaceholders();
        
        setupVault();

        getLogger().info("OnousCore enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) dataManager.saveAll();
        if (friendManager != null) friendManager.saveAll();
        if (ecoManager != null) ecoManager.save();
        getLogger().info("OnousCore disabled!");
    }

    private void initManagers() {
        this.cooldownManager = new CooldownManager();
        this.dataManager = new DataManager(this);
        this.teleportManager = new TeleportManager(this);
        this.tpaManager = new TPAManager(this);
        this.friendManager = new FriendManager(this);
        this.ecoManager = new EcoManager(this);
    }

    private void registerCommands() {
        new SpawnCommand(this);
        new WarpCommand(this);
        new TPACommand(this);
        new RTPCommand(this);
        new TPAHereCommand(this);
        
        new BalanceCommand(this);
        new PayCommand(this);
        new EcoCommand(this);
        
        new SettingsCommand(this);
        new MessageCommand(this);
        new FlyCommand(this);
        new HealCommand(this);
        new GamemodeCommand(this);
        new TimeCommand(this);
        new FriendCommand(this);
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new TeleportListener(this), this);
        getServer().getPluginManager().registerEvents(new WorldListener(this), this);
    }

    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceholderHook(this).register();
        }
    }

    private void setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().warning("Vault not found! Economy will be internal only.");
            return;
        }
        
        VaultHook hook = new VaultHook(this, ecoManager);
        getServer().getServicesManager().register(Economy.class, hook, this, ServicePriority.Highest);
        getLogger().info("Vault Economy Hooked!");
    }

    public static OnousCore get() { return instance; }
    public DataManager getData() { return dataManager; }
    public TeleportManager getTeleport() { return teleportManager; }
    public TPAManager getTPA() { return tpaManager; }
    public CooldownManager getCooldown() { return cooldownManager; }
    public FriendManager getFriends() { return friendManager; }
    public EcoManager getEcoManager() { return ecoManager; }

    public String getMessage(String path) {
        return getConfig().getString("messages." + path, "&cMessage not found: " + path);
    }

    public String getPrefix() {
        return getConfig().getString("messages.prefix", "&#FFB800⬥ &#AAAAAA");
    }

    public void reload() {
        reloadConfig();
        getLogger().info("Configuration reloaded!");
    }
}
