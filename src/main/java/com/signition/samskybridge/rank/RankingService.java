package com.signition.samskybridge.rank;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class RankingService {
    private final Main plugin;
    private final DataStore store;
    private final LevelService level;

    public RankingService(Main plugin, DataStore store, LevelService level){
        this.plugin = plugin;
        this.store = store;
        this.level = level;
    }

    public int getRankOf(UUID id){
        List<IslandData> sorted = store.all().stream()
                .sorted(Comparator.comparingInt(IslandData::getLevel).reversed()
                        .thenComparingLong(IslandData::getXp).reversed())
                .collect(Collectors.toList());
        for (int i=0;i<sorted.size();i++){
            if (sorted.get(i).getId().equals(id)) return i+1;
        }
        return -1;
    }

    public void sendTop(Player p, int n){
        List<IslandData> sorted = store.all().stream()
                .sorted(Comparator.comparingInt(IslandData::getLevel).reversed()
                        .thenComparingLong(IslandData::getXp).reversed())
                .limit(n)
                .collect(Collectors.toList());
        p.sendMessage(Text.color("&a섬 랭킹 TOP "+n));
        int i=1;
        for (IslandData d : sorted){
            p.sendMessage(Text.color("&7"+i+". &f"+d.getName()+" &7- &bLv."+d.getLevel()+" &7("+d.getXp()+" XP)"));
            i++;
        }
    }

    public void applyPrefix(Player p, int rank){
        String fmt = plugin.getConfig().getString("tab_prefix.format_dynamic","&7[ &a섬 랭킹 &f<rank>위 &7| &blv.<level> &7] &r");
        IslandData is = store.getOrCreate(p.getUniqueId(), p.getName());
        String tag = Text.color(fmt.replace("<rank>", String.valueOf(Math.max(rank, 0))).replace("<level>", String.valueOf(is.getLevel())));
        try {
            p.setPlayerListName(tag + ChatColor.RESET + p.getName());
        } catch (Throwable ignored){}
    }

    public void refreshTabPrefixes(){
        for (Player p : Bukkit.getOnlinePlayers()){
            int r = getRankOf(p.getUniqueId());
            applyPrefix(p, r);
        }
    }
}
