package com.signition.samskybridge.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DataStore {
    private final Plugin plugin;
    private final File file;
    private YamlConfiguration yaml;

    public DataStore(Plugin plugin){
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "data.yml");
        if (!file.exists()) {
            try { plugin.getDataFolder().mkdirs(); file.createNewFile(); } catch (IOException ignored){}
        }
        this.yaml = YamlConfiguration.loadConfiguration(file);
    }

    public void save(){ try { yaml.save(file); } catch (IOException ignored) {} }

    public int getLevel(java.util.UUID id, String type){ return yaml.getInt("levels." + id + "." + type, 1); }
    public void setLevel(java.util.UUID id, String type, int lv){ yaml.set("levels." + id + "." + type, lv); save(); }
    public long getXP(java.util.UUID id){ return yaml.getLong("xp." + id, 0L); }
    public void addXP(java.util.UUID id, long delta){ yaml.set("xp." + id, getXP(id)+delta); save(); }

    public java.util.List<java.util.UUID> listPlayers(){
        java.util.Set<String> keys = yaml.getConfigurationSection("xp") != null ? yaml.getConfigurationSection("xp").getKeys(false) : java.util.Collections.<String>emptySet();
        java.util.List<java.util.UUID> out = new java.util.ArrayList<java.util.UUID>();
        for (String k : keys) try { out.add(java.util.UUID.fromString(k)); } catch (IllegalArgumentException ignored){}
        return out;
    }
}
