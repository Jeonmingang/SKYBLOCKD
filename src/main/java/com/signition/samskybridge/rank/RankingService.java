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

    /** /섬 랭킹 출력 */
    public void sendTop(Player p, int page){
        List<IslandData> list = getSortedIslands();
        int pageSize = plugin.getConfig().getInt("ranking.page-size", 10);
        int total = list.size();
        int pages = Math.max(1, (int)Math.ceil(total / (double)pageSize));
        if (page < 1) page = 1;
        if (page > pages) page = pages;
        int from = Math.max(0, (page-1) * pageSize);
        int to = Math.min(total, from + pageSize);

        p.sendMessage(Text.color("&6[ 섬 랭킹 ] &7페이지 " + page + "/" + pages));

        for (int i = from; i < to; i++){
            IslandData is = list.get(i);
            UUID id = is.getOwner();
            String ownerName = is.getOwnerName();
            int level = is.getLevel();
            long xp = is.getXp();
            int size = is.getSize();
            int teamMax = is.getTeamMax();
            int mineLv = plugin.getFeatures().getMineLevel(id);
            int farmLv = plugin.getFeatures().getFarmLevel(id);
            String line = plugin.getConfig().getString("ranking.format.line",
                "&f{rank}. &e{owner} &7[섬:{name}] &7Lv{level} &8XP {xp} &7| 팀 {team} &7| 크기 R{size} &7| 광산 Lv{mine} &7| 농장 Lv{farm}");
            line = line.replace("{rank}", String.valueOf(i+1))
                       .replace("{owner}", ownerName==null?"-":ownerName)
                       .replace("{name}", is.getName()==null?"-":is.getName())
                       .replace("{level}", String.valueOf(level))
                       .replace("{xp}", String.valueOf(xp))
                       .replace("{team}", String.valueOf(teamMax))
                       .replace("{size}", String.valueOf(size))
                       .replace("{mine}", String.valueOf(mineLv))
                       .replace("{farm}", String.valueOf(farmLv));
            p.sendMessage(Text.color(line));
        }
        p.sendMessage(Text.color("&7총 " + total + "개 섬"));
    }

    public void refreshRanking(){ /* no-op */ }
}
