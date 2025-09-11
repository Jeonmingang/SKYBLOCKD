package com.signition.samskybridge.rank;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Minimal replacement focusing on tab prefix formatting for unranked islands.
 * If you already have your own RankingService logic, apply the rankLabel snippet.
 */
public class RankingService {
    private final Main plugin;
    private final DataStore store;

    public RankingService(Main plugin, DataStore store){
        this.plugin = plugin;
        this.store = store;
    }

    /** Dummy rank resolver; replace with your existing implementation. */
    public int getRank(UUID owner){
        return -1;
    }

    public void refreshPlayer(Player p, IslandData is){
        if (p == null || is == null) return;

        int r = getRank(is.getOwner()); // your existing ranking logic
        String unranked = plugin.getConfig().getString("ranking.unranked-label", "등록안됨");
        String rankLabel = (r < 0 ? unranked : (String.valueOf(r) + "위"));

        String fmt = plugin.getConfig().getString("ranking.tab-prefix", "&7[ &a섬 랭킹 &f<rank> &7| &bLv.<level> &7] &r");
        String prefix = Text.color(fmt.replace("<rank>", rankLabel).replace("<level>", String.valueOf(is.getLevel())));

        Scoreboard sb = p.getScoreboard();
        if (sb == null) sb = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = ("is_" + p.getUniqueId().toString().replace("-", "")).substring(0, Math.min(14, ("is_"+p.getUniqueId().toString().replace("-", "")).length()));
        Team t = sb.getTeam(teamName);
        if (t == null) t = sb.registerNewTeam(teamName);
        t.setPrefix(prefix);
        if (!t.hasEntry(p.getName())) t.addEntry(p.getName());
        p.setPlayerListName(prefix + p.getName());
    }
}