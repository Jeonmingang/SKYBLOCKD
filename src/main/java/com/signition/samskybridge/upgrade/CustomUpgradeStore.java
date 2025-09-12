
package com.signition.samskybridge.upgrade;

import com.signition.samskybridge.Main;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Persist per-island custom upgrade steps without touching IslandData/DataStore.
 * File: upgrades.yml
 */
public class CustomUpgradeStore {
    private final Main plugin;
    private final File file;
    private final YamlConfiguration yml;
    // cache: islandId -> (upgradeId -> step)
    private final Map<UUID, Map<String, Integer>> cache = new HashMap<UUID, Map<String, Integer>>();

    public CustomUpgradeStore(Main plugin){
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "upgrades.yml");
        this.yml = YamlConfiguration.loadConfiguration(file);
        load();
    }

    private void load(){
        cache.clear();
        if (yml.getConfigurationSection("islands") == null) return;
        for (String k : yml.getConfigurationSection("islands").getKeys(false)){
            try{
                UUID id = UUID.fromString(k);
                Map<String,Integer> map = new HashMap<String,Integer>();
                for (String upId : yml.getConfigurationSection("islands."+k).getKeys(false)){
                    map.put(upId, yml.getInt("islands."+k+"."+upId, 0));
                }
                cache.put(id, map);
            }catch (Throwable ignored){}
        }
    }

    public int getStep(UUID islandId, String upgradeId){
        Map<String,Integer> map = cache.get(islandId);
        if (map == null) return 0;
        Integer v = map.get(upgradeId);
        return v == null ? 0 : v;
    }

    public void setStep(UUID islandId, String upgradeId, int step){
        Map<String,Integer> map = cache.get(islandId);
        if (map == null){ map = new HashMap<String,Integer>(); cache.put(islandId, map); }
        map.put(upgradeId, step);
        yml.set("islands."+islandId.toString()+"."+upgradeId, step);
        try { yml.save(file); } catch (IOException ignored) {}
    }

    public void incStep(UUID islandId, String upgradeId){
        setStep(islandId, upgradeId, getStep(islandId, upgradeId)+1);
    }
}
