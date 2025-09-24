package com.signition.samskybridge.rank;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import java.util.*;

public class RankingService {
    private final Main plugin;
    private final DataStore store;

    public RankingService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
    }

    public List<IslandData> getSortedIslands(){
        List<IslandData> out = new ArrayList<IslandData>(this.store.all());
        Collections.sort(out, new Comparator<IslandData>(){
            public int compare(IslandData a, IslandData b){
                int c = Integer.compare(b.getLevel(), a.getLevel());
                if (c != 0) return c;
                return Long.compare(b.getXp(), a.getXp());
            }
        });
        return out;
    

    public void refreshRanking() {
        // no-op or recompute internal caches if present in other builds
    }

    public void sendTop(org.bukkit.entity.Player p, int page) {
        java.util.List<com.signition.samskybridge.data.IslandData> list = getSortedIslands();
        int pageSize = 10;
        int total = list.size();
        int from = Math.max(0, (page-1)*pageSize);
        int to = Math.min(total, from+pageSize);
        p.sendMessage(com.signition.samskybridge.util.Text.color("&6[ 섬 랭킹 ] &7페이지 " + page));
        for (int i=from; i<to; i++) {
            com.signition.samskybridge.data.IslandData is = list.get(i);
            p.sendMessage(com.signition.samskybridge.util.Text.color("&f" + (i+1) + ". &e" + is.getOwnerName() + " &7Lv" + is.getLevel() + " &8XP " + is.getXp()));
        }
        p.sendMessage(com.signition.samskybridge.util.Text.color("&7총 " + total + "개 섬"));
    }
}
