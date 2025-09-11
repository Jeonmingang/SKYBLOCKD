
package com.signition.samskybridge.rank;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class RankingService {
    private final Main plugin;
    private final DataStore data;

    public RankingService(Main plugin, DataStore data){
        this.plugin = plugin;
        this.data = data;
    
    /** Backward-compat: some code calls getRank(UUID) */
    public int getRank(java.util.UUID uid){ return rankOf(uid); }
}

    public int rankOf(UUID id){
        // sort by level desc, then xp desc
        List<Map.Entry<UUID, IslandData>> sorted = new ArrayList<>(data.all().entrySet());
        sorted.sort((a,b)->{
            int lv = Integer.compare(b.getValue().level, a.getValue().level);
            if (lv != 0) return lv;
            return Integer.compare(b.getValue().xp, a.getValue().xp);
        });
        for (int i=0;i<sorted.size();i++){
            if (sorted.get(i).getKey().equals(id)) return i+1;
        }
        return -1;
    }

    public String tabPrefixFor(Player p){
        String fmt = plugin.getConfig().getString("ranking.tab-prefix.format",
                "&7[ &a섬 랭킹 &f<rank>위 &7| &bLv.<level> &7] &r");
        int level = plugin.getLevelService().levelOf(p);
        int rank = rankOf(p.getUniqueId());
        String unrank = plugin.getConfig().getString("ranking.unranked_label", "등록안됨");
        String rankStr = rank < 0 ? unrank : String.valueOf(rank);
        return fmt.replace("<rank>", rankStr).replace("<level>", String.valueOf(level));
    }
}
