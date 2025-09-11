
package com.signition.samskybridge.data;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataStore {
    private final JavaPlugin plugin;
    private final File file;
    private final FileConfiguration yaml;

    private final Map<UUID, IslandData> players = new HashMap<>();

    public DataStore(JavaPlugin plugin){
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "players.yml");
        if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
        if (!file.exists()){
            try { file.createNewFile(); } catch (IOException ignored) {}
        }
        this.yaml = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public synchronized IslandData getOrCreate(UUID id){
        IslandData d = players.get(id);
        if (d == null) {
            d = new IslandData();
            players.put(id, d);
        }
        return d;
    }

    public synchronized Map<UUID, IslandData> all(){ return players; }

    public synchronized void addXp(UUID id, int amount){
        IslandData d = getOrCreate(id);
        d.xp += amount;
    }

    public synchronized void setLevel(UUID id, int level){
        getOrCreate(id).level = level;
    }

    public synchronized void save(){
        for (Map.Entry<UUID, IslandData> e : players.entrySet()){
            String k = e.getKey().toString();
            yaml.set(k + ".xp", e.getValue().xp);
            yaml.set(k + ".level", e.getValue().level);
        }
        try { yaml.save(file); } catch (IOException ignored){}
    }

    private synchronized void load(){
        for (String key : yaml.getKeys(false)){
            UUID id;
            try { id = UUID.fromString(key); }
            catch (IllegalArgumentException ex){ continue; }
            IslandData d = new IslandData();
            d.xp = yaml.getInt(key + ".xp", 0);
            d.level = yaml.getInt(key + ".level", 0);
            players.put(id, d);
        }
    }
}

    // --- Added for build stability (stubs) ---
    public java.util.Optional<IslandData> findByMember(java.util.UUID uid){
        try { return java.util.Optional.ofNullable(players.get(uid)); }
        catch (Throwable t){ return java.util.Optional.empty(); }
    }

    public java.util.Collection<IslandSale> getMarket(){
        return java.util.Collections.emptyList();
    }

    public static final class IslandSale {
        public final java.util.UUID islandId;
        public final String ownerName;
        public final long price;
        public final long listedAt;
        public IslandSale(java.util.UUID islandId, String ownerName, long price, long listedAt){
            this.islandId = islandId; this.ownerName = ownerName; this.price = price; this.listedAt = listedAt;
        }
    }


