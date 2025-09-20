package com.signition.samskybridge.data;

import com.signition.samskybridge.Main;
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
    private final Map<UUID, IslandData> islands = new ConcurrentHashMap<>();

    public DataStore(Main plugin){
        this.plugin = plugin;
        File dir = new File(plugin.getDataFolder(), "data");
        if (!dir.exists()) dir.mkdirs();
        this.dataFile = new File(plugin.getDataFolder(), plugin.getConfig().getString("storage.data-file","data/islands.yml"));
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        load();
    }

    private void load(){
        if (!data.contains("islands")) return;
        for (String key : data.getConfigurationSection("islands").getKeys(false)) {
            UUID id = UUID.fromString(key);
            int level = data.getInt("islands."+key+".level", 1);
            long xp = data.getLong("islands."+key+".xp", 0L);
            int size = data.getInt("islands."+key+".size", defaultSize());
            int team = data.getInt("islands."+key+".team", plugin.getConfig().getInt("upgrade.team.base-members", 2));
            String name = data.getString("islands."+key+".name","섬");
            islands.put(id, new IslandData(id, name, level, xp, size, team));
        }
    }

    public synchronized void save(){
        for (IslandData v : islands.values()){
            String base = "islands."+v.getId().toString();
            data.set(base+".level", v.getLevel());
            data.set(base+".xp", v.getXp());
            data.set(base+".size", v.getSize());
            data.set(base+".team", v.getTeamMax());
            data.set(base+".name", v.getName());
        }
        try {
            data.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save data: "+e.getMessage());
        }
    }

    public IslandData getOrCreate(UUID id, String defaultName){
        return islands.computeIfAbsent(id, k -> new IslandData(id, defaultName, 1, 0L, plugin.getConfig().getInt("upgrade.size.base-radius", 25), plugin.getConfig().getInt("upgrade.team.base-members", 2)));
    }

    public Collection<IslandData> all(){ return islands.values(); }
    private int defaultSize(){
        int blocks = plugin.getConfig().getInt("upgrade.size.base-blocks", -1);
        if (blocks > 0) return Math.max(1, (int)Math.round(blocks / 2.0));
        return plugin.getConfig().getInt("upgrade.size.base-range", plugin.getConfig().getInt("upgrade.size.base-radius", 25));
    }

}