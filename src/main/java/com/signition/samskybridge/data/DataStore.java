package com.signition.samskybridge.data;

import com.signition.samskybridge.Main;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private final Main plugin;
    private final File dataFile;
    private final FileConfiguration data;
    private final Map<UUID, IslandData> islands = new ConcurrentHashMap<UUID, IslandData>();

    public DataStore(Main plugin){
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.getParentFile().exists()) dataFile.getParentFile().mkdirs();
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        load();
    }

    public synchronized void load(){
        islands.clear();
        ConfigurationSection sec = data.getConfigurationSection("islands");
        if (sec != null){
            for (String key : sec.getKeys(false)){
                try{
                    UUID id = UUID.fromString(key);
                    String base = "islands."+key+".";
                    String name = data.getString(base+"name", "Player");
                    int level = data.getInt(base+"level", 0);
                    long xp = data.getLong(base+"xp", 0L);
                    int size = data.getInt(base+"size", 0);
                    int team = data.getInt(base+"teamMax", 1);
                    IslandData d = new IslandData(id, name, level, xp, size, team);
                    islands.put(id, d);
                }catch (Throwable ignored){}
            }
        }
    }

    public synchronized void save(){
        try{
            data.set("islands", null);
            for (Map.Entry<UUID, IslandData> e : islands.entrySet()){
                String base = "islands."+ e.getKey().toString() + ".";
                IslandData d = e.getValue();
                data.set(base+"name", d.getName());
                data.set(base+"level", d.getLevel());
                data.set(base+"xp", d.getXP());
                data.set(base+"size", d.getSize());
                data.set(base+"teamMax", d.getTeamMax());
            }
            data.save(dataFile);
        }catch (IOException ignored){}
    }

    public List<UUID> listPlayers(){
        return new ArrayList<UUID>(islands.keySet());
    }

    public IslandData get(UUID id){
        return islands.get(id);
    }

    public void put(IslandData d){
        if (d != null) islands.put(d.getId(), d);
    }

    public IslandData getOrCreate(UUID id, String name){
        IslandData d = get(id);
        if (d == null){
            d = new IslandData(id, name == null ? "Player" : name, 0, 0L, 0, 1);
            put(d);
        }
        return d;
    }

    public long getXP(UUID id){
        IslandData d = get(id);
        return d == null ? 0L : d.getXP();
    }

    public void addXP(UUID id, long delta){
        IslandData d = getOrCreate(id, "Player");
        long next = Math.max(0L, d.getXP() + delta);
        d.setXp(next);
        save();
    }
}
