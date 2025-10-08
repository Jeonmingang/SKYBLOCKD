
package com.signition.samskybridge.rank;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.util.Text;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/** Very light UI: prints top N ranks to chat to guarantee functionality. */
public class RankingUiService {
    private final Main plugin;
    private final RankingService ranking;
    private final LevelService level;
    private final DataStore store;

    public RankingUiService(Main plugin, RankingService ranking, LevelService level, DataStore store){
        this.plugin = plugin;
        this.ranking = ranking;
        this.level = level;
        this.store = store;
    }

    public void openOrRefresh(Player p){
        // Build top list on the fly from DataStore to avoid relying on scoreboard, etc.
        List<IslandData> all = store.all();
        int count = Math.max(1, plugin.getConfig().getInt("ranking.top-count", 10));
        List<IslandData> top = all.stream()
                .sorted(Comparator.comparingInt(IslandData::getLevel).reversed())
                .limit(count)
                .collect(Collectors.toList());

        p.sendMessage(Text.color("&6[섬 랭킹 TOP 10]"));
        int i = 1;
        for (IslandData is : top){
            String name = is.getName() != null ? is.getName() : (is.getOwner()!=null ? is.getOwner().toString().substring(0,8) : "unknown");
            int size = is.getSize();
            int team = is.getTeamMax();
            int lv2 = is.getLevel();
            int rankNum = i;
            int baseRange = plugin.getConfig().getInt("upgrade.size.base-range", 120);
            int deltaSize = Math.max(0, size - baseRange);
            String fmt = plugin.getConfig().getString("ranking.line-format", "&a섬 랭킹 <rank>위 &f<name> &7[Lv.<level>] &8| &7크기:&f<size>&7(+<delta_size>) &8| &7인원:&f<team>");
            String line = fmt.replace("<rank>", String.valueOf(rankNum))
                             .replace("<name>", name)
                             .replace("<level>", String.valueOf(lv2))
                             .replace("<size>", String.valueOf(size))
                             .replace("<delta_size>", String.valueOf(deltaSize))
                             .replace("<team>", String.valueOf(team));
            p.sendMessage(Text.color(line));
            i++;
        }
        // If player's island exists, also show its rank
        IslandData mine = level.getIslandOf(p);
        if (mine != null){
            int myRank = 1 + (int) all.stream()
                    .filter(o -> o != mine)
                    .filter(o -> o.getLevel() > mine.getLevel())
                    .count();
            p.sendMessage(Text.color("&7내 섬 현재 순위: &e#" + myRank + " &7( Lv.&f" + mine.getLevel() + "&7 )"));
        }
    }
}
