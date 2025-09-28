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

public boolean isEnabled(){
        try {
            org.bukkit.plugin.Plugin p = this.plugin.getServer().getPluginManager().getPlugin("BentoBox");
            return p != null && p.isEnabled();
        } catch (Throwable t){
            return false;
        }
    }

public void applyRangeInstant(org.bukkit.entity.Player player, int size){
        if (player == null) return;
        if (this.store != null){
            com.signition.samskybridge.data.IslandData d = this.store.get(player.getUniqueId());
            if (d != null){
                try { d.setSize(size); } catch (Throwable ignored){}
            }
        }
        // 실제 Bento 존재 시: 여기서 리플렉션으로 섬 보호범위 확장 로직을 넣을 수 있음(런타임 폴백은 no-op)
    }

public void applyTeamMax(org.bukkit.entity.Player player, int max){
        if (player == null) return;
        if (this.store != null){
            com.signition.samskybridge.data.IslandData d = this.store.get(player.getUniqueId());
            if (d != null){
                try { d.setTeamMax(max); } catch (Throwable ignored){}
            }
        }
    }

public void reapplyOnJoin(org.bukkit.entity.Player player, int currentRange){
        if (player == null) return;
        if (this.store != null){
            com.signition.samskybridge.data.IslandData d = this.store.get(player.getUniqueId());
            if (d != null){
                try { if (currentRange > 0) d.setSize(currentRange); } catch (Throwable ignored){}
            }
        }
        // 포션/권한/범위 등 재적용 위치(실 서버에서 Bento/권한 플러그인 존재 시 리플렉션으로 확장 가능)
    }

public boolean isMemberOrOwnerAt(org.bukkit.entity.Player p, org.bukkit.Location loc){
    try {
        // runtime reflection to Bento/BSkyBlock can be placed here; default allow true if island context unknown
        return isOwner(p, loc) || isMember(p, loc);
    } catch (Throwable t){
        return true;
    }
}




}
