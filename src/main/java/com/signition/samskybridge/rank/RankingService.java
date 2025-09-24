package com.signition.samskybridge.rank;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.entity.Player;

import java.util.*;

public class RankingService {
    private final Main plugin;
    private final DataStore store;

    public RankingService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
    }

    /** Returns islands sorted by level desc then xp desc */
    public List<IslandData> getSortedIslands(){
        List<IslandData> list = new ArrayList<>(store.all());
        Collections.sort(list, new Comparator<IslandData>(){
            @Override public int compare(IslandData a, IslandData b){
                int c = Integer.compare(b.getLevel(), a.getLevel());
                if (c != 0) return c;
                return Long.compare(b.getXp(), a.getXp());
            }
        });
        return list;
    }

    
    /** Optional helper for /섬 랭킹 간단 출력 */
    public void sendTop(Player p, int page){
        List<IslandData> list = getSortedIslands();
        int pageSize = 10;
        int total = list.size();
        int from = Math.max(0, (page-1)*pageSize);
        int to = Math.min(total, from+pageSize);
        p.sendMessage(Text.color("&6[ 섬 랭킹 ] &7페이지 " + page));
        for (int i = from; i < to; i++){
            IslandData is = list.get(i);
            String ownerName = is.getOwnerName();
            p.sendMessage(Text.color("&f" + (i+1) + ". &e" + (ownerName==null?"-":ownerName) + " &7Lv" + is.getLevel() + " &8XP " + is.getXp()));
        }
        p.sendMessage(Text.color("&7총 " + total + "개 섬"));
    }
    

    public void refreshRanking(){ /* no-op */ }
}