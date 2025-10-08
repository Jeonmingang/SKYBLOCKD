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

/** Prints top N ranks to chat with configurable line format. */
public class RankingUiService {
    private final Main plugin;
    private final DataStore store;
    private final LevelService level;
    private final RankingService ranking; // optional reference, may be null

    public RankingUiService(Main plugin, RankingService ranking, LevelService level, DataStore store){
        this.plugin = plugin;
        this.ranking = ranking;
        this.level = level;
        this.store = store != null ? store : plugin.getDataStore();
    }
    public RankingUiService(Main plugin, LevelService level, DataStore store){
        this(plugin, null, level, store);
    }
    public RankingUiService(Main plugin, DataStore store, LevelService level){
        this(plugin, null, level, store);
    }

    public void openOrRefresh(Player p){
        DataStore st = (this.store != null ? this.store : plugin.getDataStore());
        List<IslandData> all = st.all();
        int count = Math.max(1, plugin.getConfig().getInt("ranking.top-count", 10));
        List<IslandData> top = all.stream()
                .sorted(Comparator.comparingInt(IslandData::getLevel).reversed())
                .limit(count)
                .collect(Collectors.toList());

        p.sendMessage(Text.color("&6[섬 랭킹 TOP " + count + "]"));
        int i = 1;
        for (IslandData is : top){
            String name = is.getName() != null ? is.getName() :
                    (is.getOwner()!=null ? is.getOwner().toString().substring(0,8) : "unknown");
            int size = is.getSize();
            int team = is.getTeamMax();
            int baseRange = plugin.getConfig().getInt("upgrade.size.base-range", 120);
            int deltaSize = Math.max(0, size - baseRange);
            String fmt = plugin.getConfig().getString("ranking.line-format",
                    "&a섬 랭킹 <rank>위 &f<name> &7[Lv.<level>] &8| &7크기:&f<size>&7(+<delta_size>) &8| &7인원:&f<team>");
            String line = fmt.replace("<rank>", String.valueOf(i))
                    .replace("<name>", name)
                    .replace("<level>", String.valueOf(is.getLevel()))
                    .replace("<size>", String.valueOf(size))
                    .replace("<team>", String.valueOf(team))
                    .replace("<delta_size>", String.valueOf(deltaSize));
            p.sendMessage(Text.color(line));
            i++;
        }
    }
}
