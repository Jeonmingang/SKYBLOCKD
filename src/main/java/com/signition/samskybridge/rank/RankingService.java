
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

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Ranking + tab prefix rendering. Java 11 / 1.16.5 safe.
 */
public class RankingService {

    private final Main plugin;
    private final DataStore store;
    private final LevelService level;

    // rank cache: owner -> 1-based rank
    private final Map<UUID, Integer> rankCache = new ConcurrentHashMap<>();

    public RankingService(Main plugin, DataStore store, LevelService level) {
        this.plugin = plugin;
        this.store = store;
        this.level = level;
    }

    /** Recompute ranking and cache it. Tolerates missing APIs via reflection. */
    public void refreshRanking() {
        Map<UUID, IslandData> map = fetchAllIslands();
        java.util.List<IslandData> sorted = map.values().stream()
                .sorted(Comparator.<IslandData>comparingInt(IslandData::getLevel).reversed()
                        .thenComparingLong(IslandData::getXp).reversed())
                .collect(Collectors.toList());
        rankCache.clear();
        int i = 1;
        for (IslandData is : sorted) rankCache.put(is.getId(), i++);
    }

    /** Return 1-based rank of the island by level. If unknown, return -1. */
    public int getRank(UUID owner) {
        Integer r = rankCache.get(owner);
        if (r != null) return r;
        refreshRanking();
        return rankCache.getOrDefault(owner, -1);
    }

    /** /섬 랭킹 출력 */
    public void sendTop(Player p, int count) {
        if (count <= 0) count = 10;
        refreshRanking();
        Map<UUID, IslandData> map = fetchAllIslands();
        java.util.List<IslandData> sorted = map.values().stream()
                .sorted(Comparator.<IslandData>comparingInt(IslandData::getLevel).reversed()
                        .thenComparingLong(IslandData::getXp).reversed())
                .limit(count)
                .collect(Collectors.toList());

        p.sendMessage(Text.color(plugin.getConfig().getString("ranking.header", "&b섬 랭킹 Top " + count)));
        int i = 1;
        for (IslandData is : sorted) {
            String line = plugin.getConfig().getString("ranking.line",
                    "&7섬 &e<name> &7- &bLv.<level>");
            p.sendMessage(Text.color(line
                    .replace("<rank>", String.valueOf(i++))
                    .replace("<name>", is.getName())
                    .replace("<level>", String.valueOf(is.getLevel()))));
        }
    }

    /** Backwards compatibility: some call sites used this name. */
    public void updateTabPrefix(Player p, int rank, boolean isLeader) {
        applyPrefix(p, rank, isLeader);
    }

    /** Backwards compatibility overload. */
    public void applyPrefix(Player p, int rank) {
        applyPrefix(p, rank, false);
    }

    /**
     * Apply the dynamic [섬 랭킹 | Lv.] prefix to the player's main scoreboard team.
     * Team name is truncated to avoid the 16-char limit in 1.16.
     */
    public void applyPrefix(Player p, int rank, boolean isLeader) {
        String key = isLeader
                ? "tab_prefix.format_dynamic_leader"
                : "tab_prefix.format_dynamic_member";
        String fmt = plugin.getConfig().getString(
                key,
                plugin.getConfig().getString(
                        "tab_prefix.format_dynamic",
                        "&7[ &a섬 랭킹 &f<rank>위 &7| &bLv.<level> &7] &r"));

        String rankStr = (rank > 0)
                ? String.valueOf(rank)
                : plugin.getConfig().getString("ranking.unranked-label", "등록안됨");

        IslandData is = store.getOrCreate(p.getUniqueId(), p.getName());
        String prefix = Text.color(fmt
                .replace("<rank>", rankStr)
                .replace("<level>", String.valueOf(is.getLevel())));

        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        String teamName = "SSB_" + p.getName().substring(0, Math.min(12, p.getName().length()));
        Team team = board.getTeam(teamName);
        if (team == null) team = board.registerNewTeam(teamName);
        team.setPrefix(prefix);
        if (!team.hasEntry(p.getName())) team.addEntry(p.getName());
        p.setScoreboard(board);
    }

    /** Safely fetch all islands from DataStore even if it has no public API. */
    @SuppressWarnings("unchecked")
    private Map<UUID, IslandData> fetchAllIslands() {
        try {
            // Preferred public APIs, if present (not required)
            try {
                java.lang.reflect.Method m = store.getClass().getMethod("all");
                Object r = m.invoke(store);
                if (r instanceof Map) return (Map<UUID, IslandData>) r;
            } catch (NoSuchMethodException ignored) {}
            try {
                java.lang.reflect.Method m = store.getClass().getMethod("values");
                Object r = m.invoke(store);
                if (r instanceof Map) return (Map<UUID, IslandData>) r;
            } catch (NoSuchMethodException ignored) {}

            // Fallback: access private field 'islands'
            Field f = store.getClass().getDeclaredField("islands");
            f.setAccessible(true);
            Object v = f.get(store);
            if (v instanceof Map) {
                return (Map<UUID, IslandData>) v;
            }
        } catch (Throwable ignored) {}
        // last resort: build a map only with online players' islands
        Map<UUID, IslandData> tmp = new HashMap<>();
        for (Player op : Bukkit.getOnlinePlayers()) {
            IslandData is = store.getOrCreate(op.getUniqueId(), op.getName());
            tmp.put(is.getId(), is);
        }
        return tmp;
    }
}
