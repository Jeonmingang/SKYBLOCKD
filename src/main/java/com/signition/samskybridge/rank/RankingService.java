package com.signition.samskybridge.rank;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RankingService {
    private final Main plugin;
    private final DataStore data;

    public RankingService(Main plugin, DataStore data){
        this.plugin = plugin;
        this.data = data;
    }

    /** 플레이어(섬)의 랭크. 현재는 집계 미구현으로 -1 반환(집계중). */
    public int rankOf(UUID uid){
        return -1;
    }

    /** 과거 호환: getRank(UUID) */
    public int getRank(UUID uid){ return rankOf(uid); }

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
