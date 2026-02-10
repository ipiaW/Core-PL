package com.onous.core.data;

import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Warp Data Class
 * Stores warp information
 */
public class Warp {

    // ═══════════════════════════════════════
    // Fields
    // ═══════════════════════════════════════
    private final String name;
    private final Location location;
    private Material icon;
    private String displayName;

    // ═══════════════════════════════════════
    // Constructor
    // ═══════════════════════════════════════
    
    /**
     * Create new warp
     * 
     * @param name Warp name (identifier)
     * @param location Warp location
     */
    public Warp(String name, Location location) {
        this.name = name.toLowerCase();
        this.location = location;
        this.icon = Material.ENDER_PEARL;
        this.displayName = name;
    }

    /**
     * Create new warp with icon
     * 
     * @param name Warp name
     * @param location Warp location
     * @param icon Display icon
     */
    public Warp(String name, Location location, Material icon) {
        this.name = name.toLowerCase();
        this.location = location;
        this.icon = icon;
        this.displayName = name;
    }

    // ═══════════════════════════════════════
    // Getters
    // ═══════════════════════════════════════
    
    /**
     * Get warp name (lowercase identifier)
     */
    public String getName() {
        return name;
    }

    /**
     * Get warp location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Get display icon
     */
    public Material getIcon() {
        return icon;
    }

    /**
     * Get display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get world name
     */
    public String getWorldName() {
        if (location == null || location.getWorld() == null) {
            return "Unknown";
        }
        return location.getWorld().getName();
    }

    /**
     * Get X coordinate
     */
    public int getX() {
        return location != null ? location.getBlockX() : 0;
    }

    /**
     * Get Y coordinate
     */
    public int getY() {
        return location != null ? location.getBlockY() : 0;
    }

    /**
     * Get Z coordinate
     */
    public int getZ() {
        return location != null ? location.getBlockZ() : 0;
    }

    /**
     * Get formatted coordinates
     */
    public String getCoordinates() {
        return getX() + ", " + getY() + ", " + getZ();
    }

    // ═══════════════════════════════════════
    // Setters
    // ═══════════════════════════════════════
    
    /**
     * Set display icon
     */
    public void setIcon(Material icon) {
        if (icon != null) {
            this.icon = icon;
        }
    }

    /**
     * Set display name
     */
    public void setDisplayName(String displayName) {
        if (displayName != null && !displayName.isEmpty()) {
            this.displayName = displayName;
        }
    }

    // ═══════════════════════════════════════
    // Validation
    // ═══════════════════════════════════════
    
    /**
     * Check if warp is valid
     */
    public boolean isValid() {
        return location != null && location.getWorld() != null;
    }

    // ═══════════════════════════════════════
    // Object Methods
    // ═══════════════════════════════════════
    
    @Override
    public String toString() {
        return "Warp{" +
                "name='" + name + '\'' +
                ", world='" + getWorldName() + '\'' +
                ", coords=" + getCoordinates() +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Warp warp = (Warp) obj;
        return name.equals(warp.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
