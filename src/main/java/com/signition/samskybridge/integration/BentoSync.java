package com.signition.samskybridge.integration;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BentoSync {
    private final Plugin plugin;
    private final DataStore store;

    public BentoSync(Plugin plugin){
        this.plugin = plugin;
        DataStore s = null;
        try {
            if (plugin instanceof Main){
                java.lang.reflect.Method m = plugin.getClass().getMethod("getDataStore");
                s = (DataStore) m.invoke(plugin);
            }
        } catch (Throwable ignored){}
        this.store = s;
    }

    public boolean isOwner(Player p, Location loc){
        if (store == null || p == null) return false;
        IslandData d = store.get(p.getUniqueId());
        return d != null;
    }

    public boolean isMember(Player p, Location loc){
        // 팀 데이터 미보유 폴백
        return false;
    }

    public int getIslandLevel(Player p){
        if (store == null || p == null) return 0;
        IslandData d = store.get(p.getUniqueId());
        return d == null ? 0 : d.getLevel();
    }

    public int getIslandRank(Player p){
        if (store == null || p == null) return 0;
        List<IslandData> list = new ArrayList<IslandData>(store.all());
        Collections.sort(list, new Comparator<IslandData>(){
            public int compare(IslandData a, IslandData b){
                long xa = a.getXp();
                long xb = b.getXp();
                return xa==xb ? 0 : (xa<xb ? 1 : -1);
            }
        });
        int rank = 1;
        for (IslandData d : list){
            if (p.getUniqueId().equals(d.getId())) return rank;
            rank++;
        }
        return 0;
    }
}
