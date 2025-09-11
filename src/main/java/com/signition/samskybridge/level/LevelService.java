
package com.signition.samskybridge.level;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LevelService {
    private final Main plugin;
    private final DataStore data;
    private int base;
    private int pct;
    private int cap;
    private final Map<String, Integer> xpByKey = new HashMap<>();

    public LevelService(Main plugin, DataStore data){
        this.plugin = plugin;
        this.data = data;
        reload();
    }

    public void reload(){
        base = plugin.getConfig().getInt("level.base", 100);
        pct = plugin.getConfig().getInt("level.percent-increase", 10);
        cap = plugin.getConfig().getInt("level.cap", 50000);

        xpByKey.clear();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("xp.blocks");
        if (sec != null){
            for (String k : sec.getKeys(false)){
                xpByKey.put(k.toLowerCase(), sec.getInt(k, 0));
            }
        }
    }

    public int getXpFor(Material mat){
        String key = mat.getKey().toString().toLowerCase();
        Integer v = xpByKey.get(key);
        return v == null ? 0 : v;
    }

    public int nextRequired(int level){
        // geometric like: base * (1 + pct/100)^level (clamped)
        double req = base * Math.pow(1.0 + (pct / 100.0), Math.max(0, level));
        return Math.min((int)Math.round(req), cap);
    }

    public void grantXp(Player p, int amount){
        UUID id = p.getUniqueId();
        IslandData d = data.getOrCreate(id);
        d.xp += amount;
        // level up
        while (d.xp >= nextRequired(d.level)){
            d.xp -= nextRequired(d.level);
            d.level++;
        }
    }

    public int levelOf(Player p){ return data.getOrCreate(p.getUniqueId()).level; }
    public int xpOf(Player p){ return data.getOrCreate(p.getUniqueId()).xp; }
}
