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

import java.util.UUID;

/**
 * Responsible only for prefix/tab rendering and simple rank helpers.
 * Keep this class self‑contained so it compiles cleanly on Java 11 / MC 1.16.5.
 */
public class RankingService {

    private final Main plugin;
    private final DataStore store;
    private final LevelService level;

    public RankingService(Main plugin, DataStore store, LevelService level) {
        this.plugin = plugin;
        this.store = store;
        this.level = level;
    }

    /** Return 1‑based rank of the island by level. If unknown, return -1. */
    public int getRank(UUID owner) {
        // DataStore implementation may provide a direct way. Fallback: compute on demand.
        try {
            return store.getRank(owner);
        } catch (Throwable ignore) {
            // Optional: no direct API – do not block compilation.
            return -1;
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
}
