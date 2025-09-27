package com.signition.samskybridge.data;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {

    private final Plugin plugin;
    private final File file;
    private FileConfiguration config;

    // owner -> island data
    private final Map<UUID, IslandData> islands = new ConcurrentHashMap<>();
    // player -> owner
    private final Map<UUID, UUID> playerIndex = new ConcurrentHashMap<>();

    public DataStore(Plugin plugin){
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        if (!this.file.getParentFile().exists()) {
            this.file.getParentFile().mkdirs();
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
        loadFromDisk();
    }

    public synchronized void loadFromDisk() {
        islands.clear();
        playerIndex.clear();
        if (!file.exists()) {
            return;
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);
        ConfigurationSection root = config.getConfigurationSection("islands");
        if (root == null) return;

        for (String key : root.getKeys(false)) {
            try {
                UUID owner = UUID.fromString(key);
                ConfigurationSection s = root.getConfigurationSection(key);
                if (s == null) continue;
                String name = s.getString("name", "");
                int level = s.getInt("level", 0);
                long xp = s.getLong("xp", 0L);
                int size = s.getInt("size", 0);
                int teamMax = s.getInt("teamMax", 1);
                IslandData d = new IslandData(owner, name, level, xp, size, teamMax);
                islands.put(owner, d);

                List<String> members = s.getStringList("members");
                if (members != null) {
                    for (String m : members) {
                        try {
                            UUID pid = UUID.fromString(m);
                            playerIndex.put(pid, owner);
                        } catch (IllegalArgumentException ignored) { }
                    }
                }
                // owner is always a member of their own island
                playerIndex.put(owner, owner);
            } catch (Exception ex) {
                Bukkit.getLogger().warning("[SamSkyBridge] Invalid island id: " + key + " - " + ex.getMessage());
            }
        }
    }

    public synchronized void saveToDisk() {
        YamlConfiguration out = new YamlConfiguration();
        ConfigurationSection root = out.createSection("islands");
        for (Map.Entry<UUID, IslandData> e : islands.entrySet()) {
            UUID owner = e.getKey();
            IslandData d = e.getValue();
            ConfigurationSection s = root.createSection(owner.toString());
            s.set("name", d.getOwnerName());
            s.set("level", d.getLevel());
            s.set("xp", d.getXp());
            s.set("size", d.getSize());
            s.set("teamMax", d.getTeamMax());

            List<String> members = new ArrayList<>();
            for (Map.Entry<UUID, UUID> idx : playerIndex.entrySet()) {
                if (owner.equals(idx.getValue())) {
                    members.add(idx.getKey().toString());
                }
            }
            s.set("members", members);
        }
        try {
            out.save(file);
        } catch (IOException ex) {
            Bukkit.getLogger().warning("[SamSkyBridge] Failed to save data.yml: " + ex.getMessage());
        }
    }

    public synchronized IslandData findByOwner(UUID owner) {
        return islands.get(owner);
    }

    public synchronized IslandData findByPlayer(UUID player) {
        UUID owner = playerIndex.get(player);
        if (owner == null) {
            // If player is the owner but not indexed yet
            return islands.get(player);
        }
        return islands.get(owner);
    }

    public synchronized IslandData ensureByOwner(UUID owner) {
        IslandData d = islands.get(owner);
        if (d == null) {
            d = new IslandData(owner, "", 0, 0L, 0, 1);
            islands.put(owner, d);
        }
        playerIndex.put(owner, owner);
        return d;
    }

    public synchronized IslandData findByOwnerName(String ownerName) {
        if (ownerName == null) return null;
        for (IslandData d : islands.values()) {
            if (ownerName.equalsIgnoreCase(d.getOwnerName())) return d;
        }
        return null;
    }

    public synchronized void updateOwnerName(UUID owner, String name) {
        IslandData d = ensureByOwner(owner);
        d.setName(name);
    }

    public synchronized Collection<IslandData> all() { return islands.values(); }

    public synchronized int getSize() { return islands.size(); }

    // ---------- Compatibility API used across services ----------
    public synchronized long getXP(UUID player) {
        IslandData d = findByPlayer(player);
        return d != null ? d.getXp() : 0L;
    }

    public synchronized void addXP(UUID player, long delta) {
        IslandData d = findByPlayer(player);
        if (d == null) d = ensureByOwner(player);
        d.addXp(delta);
    }

    public synchronized Set<UUID> listPlayers() {
        return new HashSet<>(playerIndex.keySet());
    }
}
