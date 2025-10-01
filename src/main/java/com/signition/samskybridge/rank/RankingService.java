package com.signition.samskybridge.rank;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.stream.Collectors;

public class RankingService {
    private final Main plugin;
    private final DataStore store;
    private final LevelService level;

    private List<IslandData> lastTop = new ArrayList<>();

    public RankingService(Main plugin, DataStore store, LevelService level){
        this.plugin = plugin;
        this.store = store;
        this.level = level;
    }

    public void refreshRanking(){
        lastTop = store.all().stream()
                .sorted(Comparator.comparingInt(IslandData::getLevel).thenComparingLong(IslandData::getXp).reversed())
                .limit(100)
                .collect(Collectors.toList());
        for (Player p : Bukkit.getOnlinePlayers()){
            int rank = getRankOf(p.getUniqueId());
            applyPrefix(p, rank);
        }
    }

    public int getRankOf(UUID id){
        for (int i=0;i<lastTop.size();i++){
            if (lastTop.get(i).getId().equals(id)) return i+1;
        }
        return -1;
    }

    public void sendTop(Player viewer, int n){
        viewer.sendMessage(Text.color(plugin.getConfig().getString("messages.ranking.header","&a섬 랭킹 TOP <n>").replace("<n>", String.valueOf(n))));
        for (int i=0;i<Math.min(n, lastTop.size()); i++){
            IslandData is = lastTop.get(i);
            String line = plugin.getConfig().getString("messages.ranking.line",
                    "&f<rank>. &a<name> &7- &f레벨 <level> &7(경험치 <xp>)");
            line = line.replace("<rank>", String.valueOf(i+1))
                    .replace("<name>", is.getName())
                    .replace("<level>", String.valueOf(is.getLevel()))
                    .replace("<xp>", String.valueOf(is.getXp()))
                    .replace("<size>", String.valueOf(is.getSize()))
                    .replace("<team>", String.valueOf(is.getTeamMax()));
            viewer.sendMessage(Text.color(line));
        }
        int my = getRankOf(viewer.getUniqueId());
        if (my>0){
            viewer.sendMessage(Text.color(plugin.getConfig().getString("messages.ranking.your-rank","&a당신의 순위: &f<rank>위").replace("<rank>", String.valueOf(my))));
        }
    }

    

private void applyPrefix(Player p, int rank){
    world.bentobox.bentobox.BentoBox bb = world.bentobox.bentobox.BentoBox.getInstance();
    world.bentobox.bentobox.managers.IslandsManager im = (bb != null) ? bb.getIslands() : null;
    world.bentobox.bentobox.database.objects.Island isObj = (im != null) ? im.getIsland(p.getWorld(), p.getUniqueId()) : null;

    // rank string (e.g., 등록안됨)
    String rankStr = (rank > 0) ? String.valueOf(rank) : plugin.getConfig().getString("ranking.unranked-label", "등록안됨");

    String fmt;
    if (isObj == null) {
        // No island - use member format by default
        fmt = plugin.getConfig().getString("tab_prefix.format_dynamic_member",
                plugin.getConfig().getString("tab_prefix.format_dynamic", "&7[ &a섬 랭킹 &f<rank>위 &7| &blv.<level> &7] &r"));
    } else {
        boolean isLeader = isObj.getOwner() != null && isObj.getOwner().equals(p.getUniqueId());
        if (isLeader){
            fmt = plugin.getConfig().getString("tab_prefix.format_dynamic_leader",
                    plugin.getConfig().getString("tab_prefix.format_dynamic", "&7[ &a섬 랭킹 &f<rank>위 &7| &blv.<level> &7] &r"));
        }else{
            fmt = plugin.getConfig().getString("tab_prefix.format_dynamic_member",
                    plugin.getConfig().getString("tab_prefix.format_dynamic", "&7[ &a섬 랭킹 &f<rank>위 &7| &blv.<level> &7] &r"));
        }
    }

    // level from our store (0 when missing)
    com.signition.samskybridge.data.IslandData is = store.getOrCreate(p.getUniqueId(), p.getName());
    String prefix = com.signition.samskybridge.util.Text.color(fmt
            .replace("<rank>", rankStr)
            .replace("<level>", String.valueOf(is.getLevel())));

    org.bukkit.scoreboard.Scoreboard board = org.bukkit.Bukkit.getScoreboardManager().getMainScoreboard();
    String teamName = "SSB_" + p.getName().substring(0, Math.min(12, p.getName().length()));
    org.bukkit.scoreboard.Team t = board.getTeam(teamName);
    if (t == null) t = board.registerNewTeam(teamName);
    t.setPrefix(prefix);
    if (!t.hasEntry(p.getName())) t.addEntry(p.getName());
    p.setScoreboard(board);
}
}

}