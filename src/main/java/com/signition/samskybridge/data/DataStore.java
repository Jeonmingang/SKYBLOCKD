package com.signition.samskybridge.data;

import com.signition.samskybridge.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataStore {
    private final Main plugin;
    private final File dataFile;
    private final YamlConfiguration data;
    private final Map<UUID, IslandData> islands = new HashMap<>();

    public DataStore(Main plugin){
        this.plugin = plugin;
        File dir = new File(plugin.getDataFolder(), "data");
        if (!dir.exists()) dir.mkdirs();
        String path = "data/islands.yml";
        try {
            FileConfiguration cfg = plugin.getConfig();
            String p = cfg.getString("storage.data-file", "data/islands.yml");
            if (p != null && !p.trim().isEmpty()) path = p;
        } catch (Throwable ignored) {}
        this.dataFile = new File(plugin.getDataFolder(), path);
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        load();
    }

    private void load(){
        islands.clear();
        if (!data.contains("islands")) return;
        for (String key : data.getConfigurationSection("islands").getKeys(false)) {
            try {
                UUID id = UUID.fromString(key);
                String base = "islands."+key;
                String name = data.getString(base+".name", key);
                int level = data.getInt(base+".level", 1);
                long xp = data.getLong(base+".xp", 0L);
                int size = data.getInt(base+".size", 50);
                int team = data.getInt(base+".teamMax", 1);
                islands.put(id, new IslandData(id, name, level, xp, size, team));
            } catch (Throwable ignored) {}
        }
    }

    public void save(){
        data.set("islands", null);
        for (IslandData v : islands.values()){
            String base = "islands."+v.getId().toString();
            data.set(base+".name", v.getName());
            data.set(base+".level", v.getLevel());
            data.set(base+".xp", v.getXp());
            data.set(base+".size", v.getSize());
            data.set(base+".teamMax", v.getTeamMax());
        }
        try { data.save(dataFile); } catch (IOException e){ plugin.getLogger().severe("Failed to save data: "+e.getMessage()); }
    }

    public IslandData getOrCreate(UUID id, String defaultName){
        return islands.computeIfAbsent(id, k -> new IslandData(id, defaultName, 1, 0L, 50, 1));
    }

    public Collection<IslandData> all(){ return islands.values(); }
}
