package com.signition.samskybridge.level;

import com.signition.samskybridge.data.DataStore;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class LevelService {
    public int getLevel(java.util.UUID id){ long xp = store.getXP(id); int lv = 1; while (xp >= needFor(lv+1)) lv++; return lv; }
    public int calcLevelByXP(long xp){ int lv=1; while (xp >= needFor(lv+1)) lv++; return lv; }

    private final Plugin plugin;
    private final DataStore store;

    public LevelService(Plugin plugin, DataStore store){
        this.plugin = plugin; this.store = store;
    }

    public long needFor(int level){
        long d = level-1;
        return 100 + 50*d*d;
    }

    public int getLevel(Player p){
        long xp = store.getXP(p.getUniqueId());
        int lv = 1;
        while (xp >= needFor(lv+1)) lv++;
        return lv;
    }

    public void addXP(Player p, long delta){ store.addXP(p.getUniqueId(), delta); }
    public long getXP(Player p){ return store.getXP(p.getUniqueId()); }
}
