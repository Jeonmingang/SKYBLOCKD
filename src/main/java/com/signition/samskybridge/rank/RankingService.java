package com.signition.samskybridge.rank;

import com.signition.samskybridge.Main;
import com.signition.samskybridge.data.DataStore;
import com.signition.samskybridge.data.IslandData;
import com.signition.samskybridge.level.LevelService;
import com.signition.samskybridge.util.Text;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds island rank cache and applies TAB prefix/suffix using the config template.
 * IMPORTANT: Looks up player's island by UUID via LevelService/DataStore (world-agnostic),
 * so it works even when the player is not currently in an island world.
 */
public class RankingService implements org.bukkit.event.Listener {

    private final Main plugin;
    private final DataStore store;
    private final LevelService level;

    private final Map<UUID, Integer> rankCache = new ConcurrentHashMap<>();
    private volatile long lastRankBuild = 0L;

    public RankingService(Main plugin, DataStore store, LevelService level){
        this.plugin = plugin;
        this.store = store;
        this.level = level;
    }

    /** Rebuild cache: owner UUID -> rank (1-based). */
    public synchronized void rebuildRanks(){
        try {
            List<IslandData> all = new ArrayList<>(store.all());
            all.sort(Comparator.comparingInt(IslandData::getLevel).reversed());
            rankCache.clear();
            int r = 1;
            for (IslandData is : all){
                UUID owner = is.getOwner();
                if (owner != null){
                    rankCache.put(owner, r++);
                }
            }
            lastRankBuild = System.currentTimeMillis();
        } catch (Throwable t){
            plugin.getLogger().warning("[RankingService] rebuildRanks failed: " + t.getMessage());
        }
    }

    private int getRank(UUID owner){
        if (owner == null) return 0;
        long now = System.currentTimeMillis();
        int intervalTicks = plugin.getConfig().getInt("tab.reapply-interval-ticks", 200);
        int intervalMs = Math.max(1, intervalTicks) * 50;
        if (now - lastRankBuild > intervalMs){
            rebuildRanks();
        }
        return rankCache.getOrDefault(owner, 0);
    }

    private Scoreboard mainBoard(){
        return Bukkit.getScoreboardManager().getMainScoreboard();
    }

    private String fmt(String key){
        String s = plugin.getConfig().getString(key, "");
        return Text.color(s);
    }

    private Team teamFor(Player p){
        Scoreboard sb = mainBoard();
        Team t = sb.getTeam(p.getName());
        if (t == null){
            t = sb.registerNewTeam(p.getName());
        }
        if (!t.hasEntry(p.getName())) t.addEntry(p.getName());
        return t;
    }

    /** Apply TAB prefix/suffix according to player's island role (leader/member/none). */
    public void applyTab(Player p){
        try {
            IslandData is = level.getIslandOf(p); // world-independent
            Team t = teamFor(p);

            if (is == null){
                t.setPrefix(fmt("tab.dynamic.none.prefix"));
                t.setSuffix(fmt("tab.dynamic.none.suffix"));
                return;
            }

            boolean isLeader = p.getUniqueId().equals(is.getOwner());
            int rank = getRank(is.getOwner());
            int lv = is.getLevel();
            String islandName = is.getName() == null ? "섬" : is.getName();

            if (isLeader){
                String pre = fmt("tab.dynamic.leader.prefix")
                        .replace("<rank>", String.valueOf(rank))
                        .replace("<level>", String.valueOf(lv));
                String suf = fmt("tab.dynamic.leader.suffix")
                        .replace("<level>", String.valueOf(lv));
                t.setPrefix(pre);
                t.setSuffix(suf);
            } else {
                String pre = fmt("tab.dynamic.member.prefix")
                        .replace("<island>", islandName)
                        .replace("<rank>", String.valueOf(rank));
                String suf = fmt("tab.dynamic.member.suffix")
                        .replace("<level>", String.valueOf(lv));
                t.setPrefix(pre);
                t.setSuffix(suf);
            }
        } catch (Throwable ex){
            plugin.getLogger().warning("[RankingService] applyTab error for " + p.getName() + ": " + ex.getMessage());
        }
    }

    public void applyAllOnline(){
        for (Player p : Bukkit.getOnlinePlayers()){
            applyTab(p);
        }
    }

    public void removeFromTeams(Player p){
        try {
            Team t = mainBoard().getTeam(p.getName());
            if (t != null){
                if (t.hasEntry(p.getName())) t.removeEntry(p.getName());
                t.unregister();
            }
        } catch (Throwable ignored){}
    }

    // Kept for compatibility with callers
    private void initTabTeams(){ /* no-op: team-per-player created lazily */ }

    /** Legacy API: trigger rank rebuild immediately. */
    public void refreshRanking(){
        rebuildRanks();
    }

    /** Legacy API: re-apply TAB to all online players. */
    public void refreshTabAll(){
        applyAllOnline();
    }

    /** Legacy API: send top-N islands to a player. */
    public void sendTop(org.bukkit.entity.Player to, int count){
        try {
            java.util.List<com.signition.samskybridge.data.IslandData> all = new java.util.ArrayList<>(store.all());
            all.sort(java.util.Comparator.comparingInt(com.signition.samskybridge.data.IslandData::getLevel).reversed());
            count = Math.max(1, Math.min(count, all.size()));
            to.sendMessage(com.signition.samskybridge.util.Text.color("&a[섬 랭킹 상위 " + count + "]"));
            for (int i=0; i<count; i++){
                com.signition.samskybridge.data.IslandData is = all.get(i);
                String name = is.getName() == null ? "섬" : is.getName();
                to.sendMessage(com.signition.samskybridge.util.Text.color("&7" + (i+1) + "위 &f" + name + " &8- &bLv." + is.getLevel()));
            }
        } catch (Throwable t){
            to.sendMessage("§c랭킹 표시 중 오류: " + t.getMessage());
        }
    }

}
