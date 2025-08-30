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
                    .replace("<xp>", String.valueOf(is.getXp()));
            viewer.sendMessage(Text.color(line));
        }
        int my = getRankOf(viewer.getUniqueId());
        if (my>0){
            viewer.sendMessage(Text.color(plugin.getConfig().getString("messages.ranking.your-rank","&a당신의 순위: &f<rank>위").replace("<rank>", String.valueOf(my))));
        }
    }

    private void applyPrefix(Player p, int rank){
        String fmt = plugin.getConfig().getString("scoreboard.prefix-format","&7[ &a섬 랭킹 &f<rank>위 &7] ");
        String prefix = Text.color(fmt.replace("<rank>", rank>0? String.valueOf(rank) : "-"));
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "SSB_" + p.getName().substring(0, Math.min(12, p.getName().length()));
        Team t = board.getTeam(teamName);
        if (t == null) t = board.registerNewTeam(teamName);
        t.setPrefix(prefix);
        if (!t.hasEntry(p.getName())) t.addEntry(p.getName());
        p.setScoreboard(board);
    }
}