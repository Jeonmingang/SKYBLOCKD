package com.signition.samskybridge.level;

import com.signition.samskybridge.data.DataStore;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LevelService {
    private final Plugin plugin;
    private final DataStore store;

    public LevelService(Plugin plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
    }

    public int getLevel(java.util.UUID id){
        long xp = store.getXP(id);
        int lv = 1;
        while (xp >= needFor(lv + 1)) lv++;
        return lv;
    }

    public int getLevel(Player p){
        return getLevel(p.getUniqueId());
    }

    public void addXP(java.util.UUID id, long delta){
        store.addXP(id, delta);
    }

    public void addXP(Player p, long delta){
        addXP(p.getUniqueId(), delta);
    }

    public long needFor(int level){
        // configurable thresholds: levels.xp.<level> or fallback quadratic
        String key = "levels.xp."+level;
        if (plugin.getConfig().isSet(key)){
            return plugin.getConfig().getLong(key);
        }
        // default formula: 100 * level^2
        return 100L * level * level;
    }
}
