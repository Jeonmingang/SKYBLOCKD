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
    private String applyFmt(String key, java.util.Map<String,String> ctx){
        String s = fmt(key);
        for (java.util.Map.Entry<String,String> e : ctx.entrySet()){
            s = s.replace(e.getKey(), e.getValue());
        }
        return s;
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
                java.util.Map<String,String> ctx0 = new java.util.HashMap<>();
                ctx0.put("<rank>", "0"); ctx0.put("<level>", "0"); ctx0.put("<island>", "섬");
                t.setPrefix(applyFmt("tab.dynamic.none.prefix", ctx0));
                t.setSuffix(applyFmt("tab.dynamic.none.suffix", ctx0));
                return;
            }

            boolean isLeader = p.getUniqueId().equals(is.getOwner());
            int rank = getRank(is.getOwner());
            int lv = is.getLevel();
            String islandName = is.getName() == null ? "섬" : is.getName();
            java.util.Map<String,String> ctx = new java.util.HashMap<>();
            ctx.put("<rank>", String.valueOf(rank));
            ctx.put("<level>", String.valueOf(lv));
            ctx.put("<island>", islandName);

            if (isLeader){
                String pre = applyFmt("tab.dynamic.leader.prefix", ctx);
                String suf = applyFmt("tab.dynamic.leader.suffix", ctx);
                t.setPrefix(pre);
                t.setSuffix(suf);
            } else {
                String pre = applyFmt("tab.dynamic.member.prefix", ctx);
                String suf = applyFmt("tab.dynamic.member.suffix", ctx);
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
